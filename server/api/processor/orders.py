import random
from modules.email_service import gmail
import time, traceback, datetime
import threading

from modules.helpers import config
from modules.data_acess.driver import Connector

from .carts import GetSharedCartInfo

from .misc import StoresLocationJson, UUID

__mailInstance = gmail.MailService()
__mailInstance.login('joderm.store@gmail.com', config.Config.getValue('email-password'))

def ProcessSharedOrder(userid, cartid, extraInfo):
    global __mailInstance
    
    preorder_required = False
    total_price = 0
    
    items = GetSharedCartInfo(cartid)['items']
    
    if not len(items):
        return {"message": "Cart is empty"}
    
    cursor = Connector.establishConnection().cursor()
    
    query = "select productid, sizeid, quantity from inventory where productid in ({})".format(','.join([str(item['productid']) for item in orderData]))
    
    
    rows = cursor.execute(query).fetchall()
    
    inventoriesData = {}
    
    for row in rows:
        if str(row[0]) not in inventoriesData:
            inventoriesData[str(row[0])] = {}
        inventoriesData[str(row[0])][row[1]] = row[2]
    

def ProcessPersonalOrder(userid, extraInfo):
    global __mailInstance

    preorder_required = False
    total_price = 0
    
    orderType = extraInfo.get('type', 0) # 0 --> delivery, 1 --> pickup
    
    customer_name, phone_number, location, email, branchid, date = extraInfo.get('customer_name' , None),\
        extraInfo.get('phone_number', None), extraInfo.get('location', None), \
        extraInfo.get('email', None), extraInfo.get('branchid', None), extraInfo.get('date', None)
        
    if date is not None:
        date = datetime.datetime.strptime(date, '%d-%m-%Y')
            
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

    newCartId = UUID()
    
    cursor.execute('update cart set opening = 0 where cartid = ?', (cartid, ))
    
    
    if orderType == 0:
        cursor.execute('insert into orders(cartid, paymentstatus, deliverstatus, OrderType, CustomerName, CustomerPhone, Address, Email) values (?, ?, ?, ?, ?, ?, ?, ?)', (cartid, 0, 0, orderType, customer_name, phone_number, location, email))
    else: 
        cursor.execute('insert into orders(cartid, paymentstatus, deliverstatus, OrderType, CustomerName, CustomerPhone, Address, Email, branchid, Pickuptime) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', (cartid, 0, 0, orderType, customer_name, phone_number, location, email, branchid, date))
    cursor.execute('insert into cart(cartid, cartholder) values (?, ?)', (newCartId, userid))
    
    cursor.commit()
    return {"message": "Done!"}

def GetOrdersData(userid):
    query = '''select KK.cartid, KK.totalprice, count(CartDetail.productid), KK.createdAt, KK.PaymentStatus, KK.DeliverStatus, OrderType, branchid
        from cartdetail right join (
            select K.cartid, K.totalprice, orders.CreatedAt, PaymentStatus, DeliverStatus, OrderType, branchid 
            from (select cartid, totalprice from cart c where cartholder = ? and opening = 0) K 
            left join orders on (orders.cartid = K.cartid)
        ) KK on (KK.cartid = CartDetail.cartid)
        group by KK.cartid, KK.totalprice, KK.createdAt, KK.PaymentStatus, KK.DeliverStatus, OrderType, branchid
    '''
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
    
def PersonalOrderDetails(userid, code):
    cursor = Connector.establishConnection().cursor()
    
    query = '''select o.createdAt, o.PaymentStatus, o.DeliverStatus, o.OrderType, o.branchid, c.totalprice, o.CustomerName, o.CustomerPhone, o.Address, o.Email, o.PickupTime from (
            select cartid, cartholder, totalprice from cart 
            where cartholder = ? and cartid = ? and opening = 0
        ) c left join orders o on (o.cartid = c.cartid)
    '''

    generalInfo = cursor.execute(query, (userid, code, )).fetchone()
    if not generalInfo:
        raise Exception("permission denied")
    
    query = '''
        select c.productid, c.sizeid, c.quantity, p.title, p.descriptions, p.price, p.sexid, p.categoryid, p.imageurl from (
            select productid, sizeid, quantity from cartdetail where cartid = ?
        ) c left join product p on (p.id = c.productid)
    '''

    rows = cursor.execute(query, (code, )).fetchall()
    orderType = 1 if generalInfo[3] else 0
    
    res = {
        "totalprice": generalInfo[5],
        "createdat": datetime.datetime.strftime(generalInfo[0] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[0] else None,
        "paymentstatus": generalInfo[1],
        "deliverstatus": generalInfo[2],
        "ordertype": 1 if generalInfo[3] else 0,
        "branchid": generalInfo[4],
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
    
    if not orderType:
        res['infor'] = {
            "customer_name": generalInfo[6],
            "phone_number": generalInfo[7],
            "email": generalInfo[9],
            "location": generalInfo[8],
        },
    else:
        res['date'] = datetime.datetime.strftime(generalInfo[10] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[10] else None
        
        res['customer'] = {
            "customer_name": generalInfo[6],
            "phone_number": generalInfo[7],
            "email": generalInfo[9]
        }
        
        branchList = StoresLocationJson()
        
        for branch in branchList["branchs"]:
            if branch['branch_id'] == generalInfo[4]:
                res['branch'] = branch
                break
    
    return res