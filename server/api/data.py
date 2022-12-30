import os, json, requests
import copy
import threading

from modules.helpers import config
from modules.data_acess.driver import Connector

ITEM_PER_PAGE = config.Config.getValue('item-per-page')
__categoryName = None

def GetCategoryName(code):
    global __categoryName
    if not __categoryName:
        query = 'select * from category'
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        __categoryName = {row[0]: row[1] for row in rows}
    return __categoryName[code]

__sexName = None
__asccii = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

def GetSexName(code):
    global __sexName
    if not __sexName:
        query = 'select * from Sex'
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        __sexName = {row[0]: row[1] for row in rows}
    return __sexName[code]

def GetProducts(sex, category, page = None):
    if not sex and not category:
        return {}
    
    res = {}
    
    if sex and category:
        query = 'select id, title, descriptions, price, imageurl from product where sexid = ? and categoryid = ?'
        rows = Connector.establishConnection().cursor().execute(query, (sex, category)).fetchall()
        
        res = {'data': [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'images': [row[4]],
            'category_name': GetCategoryName(category),
            'category': category,
            'sex': sex
        } for row in rows]}
        
        if page is not None:
            lIdx = page * ITEM_PER_PAGE
            rIdx = (page + 1) * ITEM_PER_PAGE
            _lIdx, _rIdx = max(lIdx, 0), min(rIdx, len(res['data']))
            res['data'] = res['data'][_lIdx : _rIdx]
        
        return res        

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
        
        if page is not None:
            
            lIdx = page * ITEM_PER_PAGE // 6
            rIdx = (page + 1) * ITEM_PER_PAGE // 6
            
            res = {
                key: val[max(lIdx, 0): min(rIdx, len(val))] for key, val in res.items()
            }
        
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
        
        if page is not None:
            
            lIdx = page * ITEM_PER_PAGE
            rIdx = (page + 1) * ITEM_PER_PAGE
            
            res = {
                key: val[max(lIdx, 0): min(rIdx, len(val))] for key, val in res.items()
            }

        return res
            
import random

__allIdsList = None

def GetProductsByListRand(limit, ignoreid):
    global __allIdsList
    if not __allIdsList:
        query = "select id from product"
        rows = Connector.establishConnection().cursor().execute(query).fetchall()
        __allIdsList = [row[0] for row in rows]

    if type(ignoreid) != list: ignoreid = [ignoreid]
    randIds = [id for id in __allIdsList if id not in ignoreid]
    randIds = random.sample(randIds, min(limit, len(randIds)))
    
    query = f"select id, title, descriptions, price, sexid, categoryid, imageurl from product p where p.id in ({','.join([str(i) for i in randIds])})"
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

def GetProductsByList_2(ids, page = None):
    if type(ids) != list or len(ids) == 0:
        return {
            'data': []
        }
    
    query = f"select id, title, descriptions, price, sexid, categoryid, imageurl from product p where p.id in ({','.join([str(i) for i in ids])})"
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    
    res = {
        'data': []
    }
    
    for row in rows:
        res['data'] += [{
            'id': row[0],
            'title': row[1],
            'description': row[2],
            'price': int(row[3]),
            'sex': row[4],
            'category': row[5],
            'images': [row[6]]
        }]
        
    if page is not None:
        lIdx, rIdx = (page) * ITEM_PER_PAGE, (page + 1) * ITEM_PER_PAGE
        res['data'] = res['data'][max(0, lIdx) : min(rIdx, len(res['data']))]

    return res
        

def GetCategoriesTree():
    query = "select sexid, categoryid from product group by sexid, categoryid"
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    
    categories = {}
    for row in rows:
        if row[0] not in categories: categories[row[0]] = []
        categories[row[0]] += [row[1]]

    return categories

def GetProductDetails(id, userid):
    queryInfo = f"select id, title, descriptions, price, sexid, categoryid from product p where p.id = ?"
    queryImage = f"select I.ImageUrl from Image I where I.ProductId = ?"
    querySize = f"select SizeId, Quantity from Inventory I where I.ProductId = ?"
    
    cursor = Connector.establishConnection().cursor()
    
    isInWishList = False
    if userid is not None:
        checkWishList = f"select count(*) from wishlist w where w.userid = ? and w.productid = ?"
        result = cursor.execute(checkWishList, (userid, id)).fetchone()
        if result[0] > 0: isInWishList = True
    
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
        'images': [item[0] for item in images],
        'isInWishList': isInWishList
    } if prodInfo else None
    
from modules.email_service import gmail

__mailInstance = gmail.MailService()
__mailInstance.login('joderm.store@gmail.com', config.Config.getValue('email-password'))

import time, traceback, datetime

def ProcessSharedOrder(cartid, extraInfo):
    pass

def ProcessOrderData(userid, extraInfo):
    global __mailInstance

    preorder_required = False
    total_price = 0
    
    orderType = extraInfo.get('type', 0) # 0 --> delivery, 1 --> pickup
    
    customer_name, phone_number, location, email, branchid, date = extraInfo.get('customer_name' , None),\
        extraInfo.get('phone_number', None), extraInfo.get('location', None), \
        extraInfo.get('email', None), extraInfo.get('branchid', None), extraInfo.get('date', None)
        
    if date is not None:
        date = datetime.datetime.strptime(date, '%d-%m-%Y')
        
    print('DEBUG\t', date)
    
    if not phone_number or not email:
        return {"message": "User info should be provided"}
    
    if orderType == 0 and (not location):
        return {"message": "Location should be provided"}
    
    if orderType == 1 and (not branchid or not date):
        return {"message": "Branch id and Date should be provided for pickup order"}
    
    query = 'select cartid from cart where cartholder = ? and opening = 1'
    cursor = Connector.establishConnection().cursor()
    cartid = cursor.execute(query, (userid, )).fetchone()
    
    if not cartid:
        return {"message": "Cart is empty"}
    
    cartid = cartid[0]
    
    query = 'select cd.productid, cd.quantity, cd.sizeid, p.price, p.imageurl, p.title from (select productid, quantity, sizeid from CartDetail where cartid = ?) cd left join product p on (p.id = cd.productid)'
    rows = cursor.execute(query, (cartid, )).fetchall()
    
    orderData = [{
        'productid': row[0],
        'quantity': row[1],
        'sizeid': row[2],
        'price': row[3],
        'imageurl': row[4],
        'title': row[5],
    } for row in rows]
    
    if len(orderData) == 0:
        return {'message': 'Cart is empty'}
    
    query = "select productid, sizeid, quantity from inventory where productid in ({})".format(','.join([str(item['productid']) for item in orderData]))
    rows = cursor.execute(query).fetchall()
    
    inventoriesData = {}
    
    for row in rows:
        if str(row[0]) not in inventoriesData:
            inventoriesData[str(row[0])] = {}
        inventoriesData[str(row[0])][row[1]] = row[2]
    
    updateTrendingQueryPattern = 'exec UpdateTrending ?, ?'
    dropDownQuery = 'update inventory set quantity = (select max(a) from (values (quantity - ?), (0)) as tmptable(a)) where productid = ? and sizeid = ?'
    
    for item in orderData:
        if inventoriesData[str(item['productid'])][item['sizeid']] < item['quantity']:
            preorder_required = True
        
        cursor.execute(dropDownQuery, (item['quantity'], item['productid'], item['sizeid']))
        total_price += item['quantity'] * item['price']

        cursor.execute(updateTrendingQueryPattern, (item['productid'], item['quantity']))
            
    content = ''

    for item in orderData:
        content += gmail.get_item_html_template(
            item['imageurl'],
            item['title'],
            item['price'],
            item['quantity'],
            item['sizeid']
        ) + '<br>\n'
    
    cost = total_price
    tax = int(total_price) * 0.06
    shipping_fee = 30000
        
    html_mail = gmail.html_mail(
        price = cost,
        tax = tax,
        shipping_fee = shipping_fee,
        html_content = content,
        product_count = len(orderData),
        customer_name = customer_name,
        phone_number = phone_number,
        location = location,
        preorder_required = preorder_required,
        orderType = orderType,
        pickupdate = date,
    )

    mailContent = gmail.build_email_content(
        'joderm.store@gmail.com', 
        [email], 
        subject = 'Đơn hàng của bạn đang trên đường vận chuyển!' if orderType == 0 else 'Lịch hẹn thử đồ!', 
        content = {'html': html_mail}
    )
    
    threading.Thread(target = __mailInstance.send_mail, args = (mailContent, )).start()

    newCartId = ''.join([__asccii[random.randint(0, len(__asccii) - 1)] for _ in range(10)])
    
    cursor.execute('update cart set opening = 0 where cartid = ?', (cartid, ))
    
    
    if orderType == 0:
        cursor.execute('insert into orders(cartid, paymentstatus, deliverstatus, OrderType, CustomerName, CustomerPhone, Address, Email) values (?, ?, ?, ?, ?, ?, ?, ?)', (cartid, 0, 0, orderType, customer_name, phone_number, location, email))
    else: 
        cursor.execute('insert into orders(cartid, paymentstatus, deliverstatus, OrderType, CustomerName, CustomerPhone, Address, Email, branchid, Pickuptime) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', (cartid, 0, 0, orderType, customer_name, phone_number, location, email, branchid, date))
    cursor.execute('insert into cart(cartid, cartholder) values (?, ?)', (newCartId, userid))
    
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

__storeDict = None

def StoresLocationJson():
    global __storeDict
    if not __storeDict:
        query = 'select locationid, X_coordinate, Y_coordinate, locationname, address from Location'
        cursor = Connector.establishConnection().cursor()
        rows = cursor.execute(query).fetchall()
        __storeDict = {
            'branchs': [{
                    'branch_id': row[0],
                    'branch_name': row[3],
                    'address': row[4],
                    'coordinate': [row[1], row[2]]
            } for row in rows] 
        }
    return __storeDict

def Profile(userid):
    query = "select * from users where id = ?"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (userid,))
    
    row = cursor.fetchone()
    
    return {
        
    } if row is not None else {
        
    }
    
def UpdateCart(userid, cartid, items):
    check = "select * from cart where userid = ? and cartid = ?"
    
    query = "select productid, quantity from cart where userid = ?"
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid,)).fetchall()
    
    currentItem = {
        row[0]: row[1] for row in rows
    }
    
    query = "insert into cart (userid, productid, quantity) values (?, ?, ?)"

    
def GetWishList(userid, page = None):
    query = 'select productid from wishlist where userid = ?'
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid,)).fetchall()
    ids = [row[0] for row in rows]
    return GetProductsByList_2(ids, page)

def AddToUserWishList(userid, productid):
    query = 'insert into wishlist (userid, productid) values (?, ?)'
    cursor = Connector.establishConnection().cursor()
    
    try:
        cursor.execute(query, (userid, productid))
        cursor.commit()
    except:
        return {
            "message": f"{productid} is already in user's wishlist!"
        }
    
    return {
        "message": "Done!"
    }
    
def RemoveFromUserWishList(userid, productid):
    query = 'delete from wishlist where userid = ? and productid = ?'
    cursor = Connector.establishConnection().cursor()
    
    try:
        cursor.execute(query, (userid, productid))
        cursor.commit()
    except:
        return {
            "message": f"{productid} has not added to user's wishlist!"
        }
    
    return {
        "message": "Removed!"
    }

def GetCartByHolder(userid):
    query = "select cartid, totalprice  from cart where cartholder = ? and opening = 1"
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (userid,)).fetchone()
    
    if not row:
        cartid = ''.join([__asccii[random.randint(0, len(__asccii) - 1)] for _ in range(10)])
        query = "insert into cart (cartid, cartholder, opening) values (?, ?, 1)"
        cursor.execute(query, (cartid, userid))
        cursor.commit()
        
        return {
            'total-price': 0,
            'details': []
        }
    
    total_price = row[1]
    cartid = row[0]
    
    query = "select cd.productid, cd.sizeid, cd.quantity, p.title, p.Descriptions, p.Price, p.SexId, p.CategoryId, p.ImageUrl from (select productid, sizeid, quantity from cartdetail c where c.cartid = ?) cd left join product p on cd.productid = p.id"
    rows = cursor.execute(query, (cartid,)).fetchall()
    
    return {
        'total-price': total_price,
        'details': [{
            'size': row[1],
            'quantity': row[2],
            'product': {
                'id': row[0],
                'title': row[3],
                'description': row[4],
                'price': row[5],
                'sex': row[6],
                'category': row[7],
                'images': [row[8]]
            }
        } for row in rows]
    }


def __updateTotalPriceBG(cursor, cartid):
    # pass the cursor as params due to threading issue
    query = "select sum(cd.quantity * p.price) from ( select productid, quantity from CartDetail where cartid = ? ) cd left join product p on cd.productid = p.id"
    total_price = cursor.execute(query, (cartid,)).fetchone()[0]
    query = "update cart set totalprice = ? where cartid = ?"
    cursor.execute(query, (total_price, cartid))
    cursor.commit()
    
import threading

def AddToCart(userid, productid, sizeid, quantity):
    query = "select cartid from cart where cartholder = ? and opening = 1"
    cursor = Connector.establishConnection().cursor()
    
    row = cursor.execute(query, (userid,)).fetchone()
    
    if not row:
        cartid = ''.join([__asccii[random.randint(0, len(__asccii) - 1)] for _ in range(10)])
        query = "insert into cart (cartid, cartholder, opening) values (?, ?, 1)"
        cursor.execute(query, (cartid, userid))
        cursor.commit()
    else: 
        cartid = row[0]
    
    query = "select quantity from cartdetail where cartid = ? and productid = ? and sizeid = ?"
    row = cursor.execute(query, (cartid, productid, sizeid)).fetchone()
    
    if not row:
        query = "insert into cartdetail (cartid, productid, sizeid, quantity) values (?, ?, ?, ?)"
        cursor.execute(query, (cartid, productid, sizeid, quantity))
    else:
        if row[0] + quantity <= 0:
            query = "delete from cartdetail where cartid = ? and productid = ? and sizeid = ?"
        else: query = "update cartdetail set quantity = ? where cartid = ? and productid = ? and sizeid = ?"
        cursor.execute(query, (row[0] + quantity, cartid, productid, sizeid))
    
    cursor.commit()
    threading.Thread(target=__updateTotalPriceBG, args = (cursor, cartid, )).start()
    
    return {
        "message": "added!"
    }

def UpdateUserWishList(userid, wishlist):
    query = 'delete from wishlist where userid = ?'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (userid, ))
    cursor.commit()
    query = 'insert into wishlist (userid, productid) values (?, ?)' # + ', '.join(['(?, ?)' for _ in range(len(wishlist))] )
    cursor.executemany(query, tuple((userid, item) for item in wishlist))
    cursor.commit()
    
    return {
        'message': "All done!"
    }
    
def GetSharedCartByUserId(userid):
    query = "select cartid from sharedcartmember where memberid = ?"
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid, )).fetchall()
    ids = [row[0] for row in rows]
    
    query = "select cartid, cartholder, opening, totalprice from sharedcart where cartid in ({})".format(', '.join(ids))

def GetSharedCartByCode(code):
    query = "select cartid, cartholder, opening, totalprice, numbersOfMembers, createdAt from sharedcart where cartid = ?"
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (code, )).fetchone()
    
    return {
        "id": row[0],
        "cartholder": row[1],
        "opening": row[2],
        "totalprice": row[3],
        "members": row[4],
    }

def ProcessMakeSharedCart(userid):
    newSharedCartId = ''.join([__asccii[random.randint(0, len(__asccii) - 1)] for _ in range(10)])
    query = "insert into sharedcart (cartid, cartholder, opening) values (?, ?, 1)"
    
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (newSharedCartId, userid))
    
    query = "insert into sharedcartmember (cartid, memberid) values (?, ?)"
    cursor.execute(query, (newSharedCartId, userid))
    cursor.commit()
    
    return {
        "Message": "New shared cart created!",
        "shared-cart-id": newSharedCartId
    }
    
def ProcessSaveCart(userid, data):
    query = 'delete from CartDetail where cartid = (select cartid from cart where cartholder = ? and opening = 1)'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (userid, ))
    cursor.commit()
    query = "select cartid, opening from cart where cartholder = ? and opening = 1"
    row = cursor.execute(query, (userid, )).fetchone()
    
    cartid = None
    if not row:
        newCartId = ''.join([__asccii[random.randint(0, len(__asccii) - 1)] for _ in range(10)])
        query = "insert into cart (cartid, cartholder) values (?, ?)"
        cursor.execute(query, (newCartId, userid))
        cartid = newCartId
    else: 
        cartid = row[0]

    query = 'insert into CartDetail (cartid, productid, sizeid, quantity) values (?, ?, ?, ?)'

    if len(data) != 0:
        cursor.executemany(query, tuple((cartid, item['productid'], item['size'], item['quantity']) for item in data))
        cursor.commit()
        threading.Thread(target=__updateTotalPriceBG, args = (cursor, cartid, )).start()
    
    return {
        "message": "saved!"
    }
    
def JoinCart(cartid, userid):  
    cursor = Connector.establishConnection().cursor()
    query = "select cartid from sharedcart where cartid = ? and opening = 1"
    row = cursor.execute(query, (cartid, )).fetchone()
    if not row:
        return {
            "message": "shared cart not found"
        }
    
    query = "select sharedcartmember where memberid = ? and cartid = ?"
    row = cursor.execute(query, (userid, cartid)).fetchone()
    if row:
        return {
            "message": "you are already in this shared cart"
        }
    try:
        query = "insert into sharedcartmember (cartid, memberid) values (?, ?)"
        cursor.execute(query, (cartid, userid))
        cursor.commit()
    except:
        return {"message": "Something went wrong"}

    return {
        "message": "joined!"
    }
    
def GetOrdersData(userid):
    query = '''select KK.cartid, KK.totalprice, count(CartDetail.productid), KK.createdAt, KK.PaymentStatus, KK.DeliverStatus, OrderType, branchid
from cartdetail right join (
    select K.cartid, K.totalprice, orders.CreatedAt, PaymentStatus, DeliverStatus, OrderType, branchid 
    from (select cartid, totalprice from cart c where cartholder = ? and opening = 0) K 
    left join orders on (orders.cartid = K.cartid)
) KK on (KK.cartid = CartDetail.cartid)
group by KK.cartid, KK.totalprice, KK.createdAt, KK.PaymentStatus, KK.DeliverStatus, OrderType, branchid'''
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid, )).fetchall()
    return {
        "orders": [
            {
                "orderid": row[0],
                "totalprice": row[1],
                "totalitems": row[2], 
                "createdat": datetime.datetime.strftime(row[3] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[3] else None,
                "paymentstatus": row[4],
                "deliverstatus": row[5],
                "ordertype": 1 if row[6] else 0,
                "branchid": row[7]
            } for row in rows
        ]
    }
    
def OrderDetails(userid, code):
    cursor = Connector.establishConnection().cursor()
    
    query = "select cartid, cartholder, totalprice from cart where cartholder = ? and cartid = ? and opening = 0"
    row = cursor.execute(query, (userid, code, )).fetchone()
    if not row:
        raise Exception("permission denied")
    
    totalprice = row[2]
    
    query = "select c.productid, c.sizeid, c.quantity, p.title, p.descriptions, p.price, p.sexid, p.categoryid, p.imageurl from (select productid, sizeid, quantity from cartdetail where cartid = ?) c left join product p on (p.id = c.productid)"
    rows = cursor.execute(query, (code, )).fetchall()
    return {
        "totalprice": totalprice,
        "details": [
            {
                "sizeid": row[1],
                "quantity": row[2],
                "product": {
                    'id': row[0],
                    'title': row[3],
                    'description': row[4],
                    'price': row[5],
                    'sex': row[6],
                    'category': row[7],
                    'images': [row[8]]
                }
            } for row in rows
        ]
    }