import random
from modules.email_service import gmail
import time, traceback, datetime
import threading

from modules.helpers import config
from modules.data_acess.driver import Connector

from .carts import GetSharedCartInfo

from .misc import StoresLocationJson, UUID, BranchInfo

__mailInstance = gmail.MailService()
__mailInstance.login('joderm.store@gmail.com', config.Config.getValue('email-password'))

def ProcessSharedOrder(userid, cartid, extraInfo):
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
    
    cursor = Connector.establishConnection().cursor()
    query = '''
    select memberid, productid, sizeid, quantity, p.title, p.Price, p.ImageUrl from
    (
        select memberid, productid, sizeid, quantity from SharedCartDetails where cartid = ?
    ) c join Product p on (p.id = c.productid)
    '''
    
    rows = cursor.execute(query, (cartid, )).fetchall()
    ordersData = {}
    ids = set([])
    
    from modules.authentication.authenticator import nameMapping
    
    consumed = {}
    
    for row in rows:
        if row[0] not in ordersData:
            ordersData[row[0]] = {
                'username': nameMapping.get(row[0], 'Unknown'),
                'products': []
            }

        ordersData[row[0]]['products'].append({
            "productid": row[1],
            "size": row[2],
            "quantity": row[3],
            "price": row[5],
            "title": row[4],
            "image": row[6]
        })
        
        ids.add(row[1])
        total_price += row[5] * row[3]
        
        if row[0] not in consumed:
            consumed[row[1]] = {
                'S': 0,
                'M': 0,
                'L': 0,
                'XL': 0,
                'XXL': 0,
            }
        
        consumed[row[1]][row[2]] += row[3]

    if not len(ids):
        return {"message": "Cart is empty"}
    
    query = 'select productid, sizeid, quantity from inventory where productid in ({})'.format(','.join([str(i) for i in ids]))
    rows = cursor.execute(query).fetchall()
    
    inventoriesData = {}
    
    for row in rows:
        if str(row[0]) not in inventoriesData:
            inventoriesData[str(row[0])] = {}
        inventoriesData[str(row[0])][row[1]] = row[2]
    
    updateTrendingQueryPattern = 'exec UpdateTrending ?, ?'
    dropDownQuery = 'update inventory set quantity = (select max(a) from (values (quantity - ?), (0)) as tmptable(a)) where productid = ? and sizeid = ?'
    
    for key, val in consumed.items():
        for size, quantity in val.items():
            if quantity != 0:
                cursor.execute(dropDownQuery, (quantity, key, size))
                cursor.execute(updateTrendingQueryPattern, (key, quantity))

    mailContentBase = '''
    <div class='main'>
        <div class="container">
            <p class="app__name">JODERN STORE</p>
            <div class='divider'></div>
        </div>
    '''

    mailContentBase += gmail.mail_greeting(
        f'Cảm ơn quý khách hàng {customer_name} đã tin tưởng và sử dụng dịch vụ của chúng tôi! Đơn hàng chung của quý khách gồm có <b>{len(list(ordersData.keys()))} thành viên</b>, chi tiết như sau:'
    )
    
    if orderType == 1:
        branchInfo = BranchInfo(branchid)
        mailContentBase += gmail.html_mail_pickup_order(
            appointmentDate = date, 
            addressname = branchInfo['branch_name'], 
            address = branchInfo['address'],
        )
        
    mailContentBase += gmail.carts_info(
        items = ordersData
    )
    
    mailContentBase += gmail.hr0()
    delivered_on = datetime.datetime.now() + datetime.timedelta(days = 5)
    mailContentBase += gmail.ord_summary(
        price = 0 if orderType == 1 else total_price, 
        shipping_fee = 0 if orderType == 1 else 30000, 
        total = (total_price + 30000) if orderType == 0 else total_price, 
        extra_note = f'Đơn hàng của quý khách sẽ được xác nhận và giao hàng trước ngày <b>{delivered_on.strftime(r"%Y-%m-%d")}</b> đến địa chỉ <b>{location}</b> và liên lạc qua số điện thoại <b>{phone_number}</b>.' if orderType == 0\
            else 'Mọi sự thay đổi thông tin xin liên hệ lại với chúng tôi qua số điện thoại <b>1723 0098</b>'
    )
    
    mailContentBase += "</div>"
    
    mailContent = gmail.build_email_content(
        'joderm.store@gmail.com', 
        [email], 
        subject = 'Đơn hàng của bạn đang trên đường vận chuyển!' if orderType == 0 else 'Lịch hẹn thử đồ!', 
        content = {'html': gmail.html_mail_2(content = mailContentBase)}
    )

    threading.Thread(target = __mailInstance.send_mail, args = (mailContent, )).start()
    
    query = 'update sharedcart set opening = 0 where cartid = ?'
    cursor.execute(query, (cartid, ))
    
    if orderType == 0:
        query = 'insert into sharedorders(cartid, customername, CustomerPhone, address, Email) values(?, ?, ?, ?, ?)'
        cursor.execute(query, (cartid, customer_name, phone_number, location, email))
        
    else:
        query = 'insert into sharedorders(cartid, customername, CustomerPhone, Email, ordertype, branchid, Pickuptime) values(?, ?, ?, ?, ?, ?, ?)'
        cursor.execute(query, (cartid, customer_name, phone_number, email, orderType, branchid, date))

    cursor.commit()
    return {
        "message": "OK"
    }
    
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
        content += gmail.item_html_template(
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




def GetSharedOrdersData(userid):
    query = '''
        select s.cartid, s.totalprice, s.totalitems, so.createdat, so.paymentstatus, so.deliverstatus, ordertype, branchid, cartname from (
            select scart.cartid, scart.totalprice, scart.totalitems, scart.cartname from (
                select * from SharedCart where opening = 0
            ) scart join (
                select * from SharedCartMember where memberid = ?
            ) smem on (smem.cartid = scart.cartid)
        ) s left join sharedorders so on (so.cartid = s.cartid)
        order by so.createdat desc
    '''
    
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid, )).fetchall()
    
    return {
        "orders": [
            {
                "orderid": row[0],
                "cartname": row[8],
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


def GetPersonalOrdersData(userid):
    query = '''select KK.cartid, KK.totalprice, count(CartDetail.productid), KK.createdAt, KK.PaymentStatus, KK.DeliverStatus, OrderType, branchid
        from cartdetail right join (
            select K.cartid, K.totalprice, orders.CreatedAt, PaymentStatus, DeliverStatus, OrderType, branchid 
            from (select cartid, totalprice from cart c where cartholder = ? and opening = 0) K 
            left join orders on (orders.cartid = K.cartid)
        ) KK on (KK.cartid = CartDetail.cartid)
        group by KK.cartid, KK.totalprice, KK.createdAt, KK.PaymentStatus, KK.DeliverStatus, OrderType, branchid
        order by KK.createdAt desc
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
        ] + GetSharedOrdersData(userid)['orders']
    }
    
def SharedOrderDetails(userid, cartid):
    cursor = Connector.establishConnection().cursor()
    
    query = '''
        select 
            createdat, paymentstatus, deliverstatus, ordertype, branchid, 
            totalprice, customername, CustomerPhone, Email, Pickuptime, cartname
        from (
            select scart.cartid, scart.totalprice, scart.totalitems, scart.cartname from (
                select * from SharedCart where opening = 0 and cartid = ?
            ) scart join (
                select * from SharedCartMember where memberid = ?
            ) smem on (smem.cartid = scart.cartid)
        ) s left join sharedorders so on (so.cartid = s.cartid)
    '''
    
    generalInfo = cursor.execute(query, (userid, )).fetchone()
    if not generalInfo:
        raise Exception("permission denied")
    
    query = '''
        select p.id, s.sizeid, s.quantity, p.Title, p.Descriptions, p.Price, p.SexId, p.CategoryId, p.ImageUrl from (
            select productid, sizeid, quantity from SharedCartDetails where cartid = ''
        ) s join Product p on ( p.Id = s.productid )
    '''
    
    rows = cursor.execute(query, (cartid, userid, )).fetchall()
    
    res = {
        "totalprice": generalInfo[5],
        "createdat": datetime.datetime.strftime(generalInfo[0] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfo[0] else None,
        "paymentstatus": generalInfo[1],
        "deliverstatus": generalInfo[2],
        "ordertype": 1 if generalInfo[3] else 0,
        "branchid": generalInfo[4],
        "cartname": generalInfo[10],
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
    
    orderType = 1 if generalInfo[3] else 0
    
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
        
        res['branch'] = BranchInfo(generalInfo[4])
        
    return res
    

def PersonalOrderDetails(userid, code):
    cursor = Connector.establishConnection().cursor()
    
    query = '''select o.createdAt, o.PaymentStatus, o.DeliverStatus, o.OrderType, o.branchid, c.totalprice, o.CustomerName, o.CustomerPhone, o.Address, o.Email, o.PickupTime from (
            select cartid, cartholder, totalprice from cart 
            where cartholder = ? and cartid = ? and opening = 0
        ) c left join orders o on (o.cartid = c.cartid)
    '''

    generalInfo = cursor.execute(query, (userid, code, )).fetchone()
    if not generalInfo:
        try:
            res = SharedOrderDetails(userid, code) # search in shared order
            return res
        except: 
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
        
        res['branch'] = BranchInfo(generalInfo[4])
        
    return res

def ProcessMarkAsDelivered(cartid):
    cursor = Connector.establishConnection().cursor()
    cursor.execute('update orders set deliverstatus = 1, paymentstatus = 1 where cartid = ?', (cartid, ))
    cursor.execute('update sharedorders set deliverstatus = 1, paymentstatus = 1 where cartid = ?', (cartid, ))
    cursor.commit()
    return {"message": "Done!"}