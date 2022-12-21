import os, json, requests
import copy
import threading

from modules.helpers import config
from modules.data_acess.driver import Connector

__categoryName = None

def GetCategoryName(code):
    global __categoryName
    if not __categoryName:
        query = 'select * from category'
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        __categoryName = {row[0]: row[1] for row in rows}
    return __categoryName[code]

__sexName = None

def GetSexName(code):
    global __sexName
    if not __sexName:
        query = 'select * from Sex'
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        __sexName = {row[0]: row[1] for row in rows}
    return __sexName[code]

def GetProducts(sex, category):
    if not sex and not category:
        return {}
    
    if sex and category:
        query = 'select id, title, descriptions, price, imageurl from product where sexid = ? and categoryid = ?'
        rows = Connector.establishConnection().cursor().execute(query, (sex, category)).fetchall()
        return [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'images': [row[4]],
            'category_name': GetCategoryName(category),
            'category': category,
            'sex': sex
        } for row in rows]
    
    if sex:
        query = 'select id, title, descriptions, price, categoryid, imageurl from product where sexid = ?'
        rows = Connector.establishConnection().cursor().execute(query, (sex, )).fetchall()
        res = {}
        for row in rows:
            if row[4] not in res: res[row[4]] = []
            res[row[4]] += [{
                'id': row[0],
                'title': row[1],
                'description': row[2],
                'price': int(row[3]),
                'images': [row[5]],
                'sex': sex,
                'category_name': GetCategoryName(row[4]),
                'category': row[4]
            }]
        return res
    
    if category:
        query = 'select id, title, descriptions, price, sexid, imageurl, categoryid from product where categoryid = ?'
        rows = Connector.establishConnection().cursor().execute(query, (category, )).fetchall()
        res = {}
        for row in rows:
            if row[4] not in res: res[row[4]] = []
            res[row[4]] += [{
                'id': row[0],
                'title': row[1],
                'description': row[2],
                'price': int(row[3]),
                'images': [row[5]],
                'category_name': GetCategoryName(category),
                'category': category,
                'sex': row[4]
            }]
        return res
            
import random

__allIdsList = None

def GetProductsByListRand(limit, ignoreid):
    global __allIdsList
    if not __allIdsList:
        query = "select id from product"
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        print(rows)
        __allIdsList = [row[0] for row in rows]

    print('DEBUG', __allIdsList)

    if type(ignoreid) != list: ignoreid = [ignoreid]
    randIds = [id for id in __allIdsList if id not in ignoreid]
    randIds = random.sample(randIds, min(limit, len(randIds)))
    
    query = f"select id, title, descriptions, price, sexid, categoryid, imageurl from product p where p.id in ({','.join([str(i) for i in randIds])})"
    print('[DEBUG]', query)
    rows = Connector.establishConnection().cursor().execute(query).fetchall()

    return [
        {
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'sex': row[4],
            'category': row[5],
            'images': [row[6]]
        } for row in rows
    ]

def GetProductsByList(ids: list, limit = 8, requiredSampler = True):
    if len(ids) > limit:
        ids = random.sample(ids, min(len(ids), limit))
  
    if len(ids) == 0:
        return {
            'data': GetProductsByListRand(limit, -1)
        }
    
    ids = [str(id) for id in ids]    
    
    idsStr = f'({", ".join(ids)})'
    query = f"select id, title, descriptions, price, sexid, categoryid, imageurl from product p where p.id in {idsStr}"
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    res = {"data": []}
    for row in rows:
        res["data"] += [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'sex': row[4],
            'category': row[5],
            'images': [row[6]]
        }]
        
    if len(res["data"]) < limit and requiredSampler:
        res["data"] += GetProductsByListRand(limit - len(res["data"]), ids[0])
    return res

def GetCategoriesTree():
    query = "select sexid, categoryid from product group by sexid, categoryid"
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    
    categories = {}
    for row in rows:
        if row[0] not in categories: categories[row[0]] = []
        categories[row[0]] += [row[1]]

    return categories

def GetProductDetails(id):
    queryInfo = f"select id, title, descriptions, price, sexid, categoryid from product p where p.id = ?"
    queryImage = f"select I.ImageUrl from Image I where I.ProductId = ?"
    querySize = f"select SizeId, Quantity from Inventory I where I.ProductId = ?"
    
    cursor = Connector.establishConnection().cursor()
    prodInfo = cursor.execute(queryInfo, (id, )).fetchone()
    images = cursor.execute(queryImage, (id, )).fetchall()
    inventory = cursor.execute(querySize, (id, )).fetchall()
    
    return {
        'id': prodInfo[0],
        'title': prodInfo[1],
        'description': prodInfo[2],
        'price': int(prodInfo[3]),
        'sex': prodInfo[4],
        'category': prodInfo[5],
        'category_name': GetCategoryName(prodInfo[5]),
        'inventory': {
            item[0]: item[1] for item in inventory
        },
        'images': [item[0] for item in images]
    }
    
from modules.email_service import gmail

__mailInstance = None

import time, traceback

def ProcessOrderData(order):
    global __mailInstance

    preorder_required = False
    total_price = 0
    
    ids = list(order['items'].keys())
    prices = {id: 0 for id in ids}
    inventories = {id: {} for id in ids}
    
    cursor = Connector.establishConnection().cursor()
    
    pricesQuery = f'select id, price from product where id in ({", ".join(ids)})'
    rows = cursor.execute(pricesQuery).fetchall()
    for row in rows:
        prices[row[0]] = row[1]
    
    inventoriesQuery = f'select ProductId, sizeid, quantity from inventory where ProductId in ({", ".join(ids)})'
    rows = cursor.execute(inventoriesQuery).fetchall()
    
    for row in rows:
        inventories[str(row[0])][row[1]] = row[2]
    
    orderedItems = {}
    updateTrendingQueryPattern = 'exec UpdateTrending ?, ?'
    dropDownQuery = 'update inventory set quantity = (select max(a) from (values (quantity - ?), (0)) as tmptable(a)) where productid = ? and sizeid = ?'
    
    for id, val in order['items'].items():
        for size, num in val.items():
            if inventories[str(id)][size] < num:
                preorder_required = True

            cursor.execute(dropDownQuery, (num, id, size))

            if id in orderedItems:
                orderedItems[id] += num 
            else: orderedItems[id] = num
            
            total_price += num * prices[id]
        
    for key, val in orderedItems.items():
        cursor.execute(updateTrendingQueryPattern, (key, val))
            
    if __mailInstance is None:
        try: 
            __mailInstance = gmail.MailService()
            __mailInstance.login('joderm.store@gmail.com', 'isowkkrraoqqihqk')
        except: __mailInstance = None
    
    generalInfo = { }
    
    getGeneralInfoQuery = 'select id, title, imageurl, price from product where product.id in ({})'.format(', '.join(ids))
    rows = cursor.execute(getGeneralInfoQuery).fetchall()
    
    for row in rows:
        generalInfo[str(row[0])] = {
            'image': row[2],
            'title': row[1],
            'price': int(row[3])
        }

    if __mailInstance:
        try:
            content = ''

            for key, value in order['items'].items():
                value_keys = list(value.keys())
                
                content += gmail.get_item_html_template(
                    generalInfo[key]['image'],
                    generalInfo[key]['title'],
                    generalInfo[key]['price'],
                    value[value_keys[0]],
                    value_keys[0]
                ) + '<br>\n'
                
            customer_name, phone_number, location = 'Unknown', 'Unknown', 'Unknown'
            
            try: customer_name = order['info']['customer_name']
            except: customer_name = ''
            
            try: phone_number = order['info']['phone_number']
            except: pass
            
            try: location = order['info']['location']
            except: pass
            
            cost = total_price
            tax = total_price * 0.06
            shipping_fee = 30000
                
            html_mail = gmail.html_mail(
                price = cost,
                tax = tax,
                shipping_fee = shipping_fee,
                html_content = content,
                product_count = len(list(order['items'].keys())),
                customer_name = customer_name,
                phone_number = phone_number,
                location = location,
                preorder_required = preorder_required
            )

            mailContent = gmail.build_email_content(
                'joderm.store@gmail.com', 
                [order.get('info', {}).get('email', None)], 
                subject = 'Đơn hàng của bạn đang trên đường vận chuyển!', 
                content = {'html': html_mail}
            )
            
            __mailInstance.send_mail(mailContent)
        except Exception as err:
            print('[EXCEPTION] Failed on sending email! Details here: ')
            traceback.print_exc()
            __mailInstance = None
            return {"message": "Something went wrong while processing your order. Please tell to us if you need any support!"}

    cursor.commit()
    return {"message": "Done!"}

def TrendingItems(top_k = 5):
    query = 'select top {} id, count from Trending order by count desc'.format(top_k)
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query).fetchall()
    ids = [row[0] for row in rows]
    return GetProductsByList(ids, top_k)
    
def HighlightItems(top_k = 5):
    query = f'select {top_k} productid from inventory group by productid order by sum(quantity) desc'
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    ids = [row[0] for row in rows]
    return GetProductsByList(ids, top_k)

def RelatedItems(id, top_k = 5):
    query = 'select categoryid from product where id = ?'
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (id,)).fetchall()
    
    if len(rows) == 0:
        return []

    catid = rows[0][0]
    
    query = f'select top {top_k * 2} id from product where categoryid = ? and id != ?'
    
    rows = cursor.execute(query, (catid, id)).fetchall()
    randList = [row[0] for row in rows]
    return GetProductsByList(random.sample(randList, min(len(randList), top_k)), top_k, False)

def ValidateOrderData(order):    
    if len(list(order.keys())) == 0:
        return {"message": "Empty cart!" , 'status': 'nOK'}
    return True

__storeList = None

def StoresLocationJson():
    global __storeList
    if not __storeList:
        query = 'select X_coordinate, Y_coordinate from Location'
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        __storeList = {'location': [[row[0], row[1]] for row in rows]}
    return __storeList

def Profile(userid):
    query = "select * from users where id = ?"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (userid,))
    
    row = cursor.fetchone()
    
    return {
        
    } if row is not None else {
        
    }
    
def GetWishList(userid):
    query = 'select productid from wishlist where userid = ?'
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid,)).fetchall()
    
    return {
        
    }