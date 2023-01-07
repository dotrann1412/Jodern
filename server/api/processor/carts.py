from modules.data_acess.driver import Connector
from .misc import UUID
import random, threading, datetime, traceback, time
from .products import categoryMapping, priceMapping

MOVE_DATA_SYNCING_TO_BACKGROUND = False
    
def ProcessFetchCartsList(userid):
    query = "select cartid, totalprice from cart where cartholder = ? and opening = 1"
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (userid,)).fetchone()
    
    if not row:
        cartid = UUID()
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
    
__backgroundQueue = [] # queries
__backgroundQueueTMP = []
__backgroundProcessing = False
__defaultQueries = [
    'delete from sharedcartdetails where quantity <= 0',
    'delete from cartdetail where quantity <= 0',
    'exec UpdateTotalItems_SharedCart',
    'exec CounterHacker'
]
    
def __background(timeout = 0.2):
    global __backgroundProcessing, __defaultQueries, __backgroundQueueTMP, __backgroundQueue

    while True:
        try:
            if len(__backgroundQueue) != 0:
                __backgroundProcessing = True
                conn = Connector.establishBackgroundConnection()
                cursor = conn.cursor()

                for query in __backgroundQueue:
                    try:
                        cursor.execute(query[0], query[1])
                        cursor.commit()
                    except Exception as e:
                        print(e)
                        traceback.print_exc()
                        cursor.rollback()
                
                for query in __defaultQueries:
                    try:
                        cursor.execute(query)
                        cursor.commit()
                    except Exception as e:
                        print(f'[ERROR] Background processing failed - {query}')
                        print(e)
                        traceback.print_exc()
                        cursor.rollback()

                __backgroundQueue = __backgroundQueueTMP
                __backgroundQueueTMP.clear()

                __backgroundProcessing = False
        except:
            print('[ERROR] Background processing failed')
            traceback.print_exc()
            __backgroundProcessing = False
            __backgroundQueue += __backgroundQueueTMP
            __backgroundQueueTMP.clear()
        
        time.sleep(timeout)
        
threading.Thread(target = __background, daemon = True).start()

def __updateTotalPriceBG(cartid):
    # pass the cursor as params due to threading issue
    query = '''
        update cart set totalprice = (
            select sum(cd.quantity * p.price) as s from ( 
                select productid, quantity from CartDetail where cartid = ?
            ) cd left join product p on cd.productid = p.id
        ) where cartid = ?
    '''
    
    if MOVE_DATA_SYNCING_TO_BACKGROUND:
        if not __backgroundProcessing: __backgroundQueue.append([query, (cartid, cartid)])
        else: __backgroundQueueTMP.append([query, (cartid, cartid)])
    else:
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query, (cartid, cartid))
        cursor.commit()
    
def __updateSharedCartBG(cartid):
    query = '''
        update SharedCart set totalprice = (
            select COALESCE(sum(quantity * Price), 0) from (
                    select *
                    from SharedCartDetails 
                    where cartid = ?
                ) sc join Product p on (p.id = sc.productid)
        ), numbersofmembers = (
            select count(*) from SharedCartMember where cartid = ?
        ) where cartid = ?
    '''

    if MOVE_DATA_SYNCING_TO_BACKGROUND:
        if not __backgroundProcessing: __backgroundQueue.append([query, (cartid, cartid, cartid)])
        else: __backgroundQueueTMP.append([query, (cartid, cartid, cartid)])
    else:
        cursor = Connector.establishConnection().cursor()
        cursor.execute(query, (cartid, cartid, cartid))
        cursor.commit()
    
def __pushLogsBG(cartid, note):
    query = 'insert into sharedcarthistory(cartid, note) values (?, ?)'

    if not __backgroundProcessing: __backgroundQueue.append([query, (cartid, note)])
    else: __backgroundQueue.append([query, (cartid, note)])
    
def AddToCart(userid, productid, sizeid, quantity, cartids = ['*']):

    if quantity == 0:
        return {"message": "Quantity cannot be zero"}

    errors = []
    
    cursor = Connector.establishConnection().cursor()
    
    from modules.authentication.authenticator import nameMapping
    username = nameMapping[userid]

    for row in cartids:
        if row == '*': continue
        query = "select count(*) as tmp from sharedcartmember where cartid = ? and memberid = ?"
        current = cursor.execute(query, (row, userid, )).fetchone()
        
        if not current:
            errors += 'Error occur while adding new item to the cart ' + row
            continue
                
        query = "select * from sharedcartdetails where cartid = ? and memberid = ? and productid = ? and sizeid = ?"
        current = cursor.execute(query, (row, userid, productid, sizeid)).fetchone()
        
        if not current:
            cursor.execute('insert into sharedcartdetails (cartid, memberid, productid, sizeid, quantity) values (?, ?, ?, ?, ?)',
                (row, userid, productid, sizeid, quantity)
            )
        else:
            cursor.execute(
                '''
                    update sharedcartdetails set quantity = (
                        select max(a) from (values (quantity + ?), (0)) as tmptable(a)
                    ) where cartid = ? and memberid = ? 
                    and productid = ? and sizeid = ?
                ''',
                (quantity, row, userid, productid, sizeid)
            )

        __updateSharedCartBG(row)

        if quantity != -1000: __pushLogsBG(row, f'{username} đã {"thêm" if quantity > 0 else "bỏ"} {quantity if quantity > 0 else -quantity} sản phẩm có mã {productid} ({categoryMapping[productid]}) size {sizeid} {"vào" if quantity > 0 else "ra khỏi"} giỏ hàng')
        else: __pushLogsBG(row, f'{username} đã ném sản phẩm có mã {productid} ({categoryMapping[productid]}) size {sizeid} ra khỏi giỏ hàng')
    
    if '*' in cartids:
        query = "select cartid from cart where cartholder = ? and opening = 1"
       
        row = cursor.execute(query, (userid,)).fetchone()
        
        if not row:
            cartid = UUID()
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
            if row[0] + quantity <= 0: query = "delete from cartdetail where cartid = ? and productid = ? and sizeid = ?"
            else: query = "update cartdetail set quantity = ? where cartid = ? and productid = ? and sizeid = ?"
            cursor.execute(query, (
                row[0] + quantity, cartid, productid, sizeid
            ) if row[0] + quantity > 0 else (cartid, productid, sizeid))
        
        cursor.commit()
        
        __updateTotalPriceBG(cartid)
    
    res = {"message": "done!"}
    if len(errors) != 0:
        res['errors'] = errors
        
    return res

def __summaryOfSharedInfo(cartid):
    query = '''select sc.cartid, sc.cartholder, sc.numbersOfMembers, sc.totalprice, sc.createdAt, Users.FullName as holdername, users.avatar as holderavatar, cartname, totalitems
        from (select * from SharedCart where cartid  = ?) sc
        join users on (
            userid = sc.cartholder
        )
    '''
    
    row = Connector.establishConnection().cursor().execute(query, (cartid, )).fetchone()
    
    return {
        "id": row[0],
        "cartholder": {
            "id": row[1],
            "name": row[5],
            "avatar": row[6]
        },
        "members": row[2],
        "totalprice": row[3],
        "createdAt": datetime.datetime.strftime(row[4] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[4] else None,
        "cartname": row[7],
        "totalitems": row[8]
    }

def ProcessMakeSharedCart(userid, cartname):
    cartid = UUID()
    query = "insert into sharedcart (cartid, cartholder, opening, totalprice, numbersOfMembers, createdAt, cartname) values (?, ?, 1, 0, 1, ?, ?)"
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (cartid, userid, datetime.datetime.now(), cartname))
    cursor.commit()
    
    query = "insert into sharedcartmember (cartid, memberid) values (?, ?)"
    cursor.execute(query, (cartid, userid))
    cursor.commit()
    
    return {
        "id": cartid,
        "info": __summaryOfSharedInfo(cartid)
    }
    
def ProcessJoinSharedCart(cartid, userid):
    cursor = Connector.establishConnection().cursor()
    query = "select cartid from sharedcart where cartid = ? and opening = 1"
    row = cursor.execute(query, (cartid, )).fetchone()

    if not row:
        return { "message": "shared cart not found" }
    
    query = "select * from SharedCartMember where memberid = ? and cartid = ?"
    row = cursor.execute(query, (userid, cartid)).fetchone()
    if row:
        return { 
            "message": "you are already in this shared cart",
            "info": __summaryOfSharedInfo(cartid)
        }
    
    try:
        query = "insert into SharedCartMember (cartid, memberid) values (?, ?)"
        cursor.execute(query, (cartid, userid))
        cursor.commit()
        threading.Thread(target=__updateSharedCartBG, args = (cartid, ), daemon = True).start()
    except: return {"message": "Something went wrong"}
    
    return { 
        "message": "joined!",
        "info": __summaryOfSharedInfo(cartid)
    }

def GetSharedCartInfo(memberid, code):
    query = '''
        select sc.cartid, SharedCart.cartholder, SharedCart.opening, SharedCart.totalprice, SharedCart.numbersOfMembers, SharedCart.createdAt, SharedCart.cartname
        from (select cartid from SharedCartMember where memberid = ? and cartid = ?) as sc 
        left join sharedcart on (sc.cartid = SharedCart.cartid)
    '''
    
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (memberid, code, )).fetchone()
    
    if not row:
        raise Exception("Cart not found")
    
    return {
        "id": row[0],
        "cartholder": row[1],
        "opening": row[2],
        "totalprice": row[3],
        "members": row[4],
        "createdat": row[5],
        "cartname": row[6],
    }
    
def ProcessSavePersonalCart(userid, data):
    query = 'delete from CartDetail where cartid = (select cartid from cart where cartholder = ? and opening = 1)'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (userid, ))
    cursor.commit()
    query = "select cartid, opening from cart where cartholder = ? and opening = 1"
    row = cursor.execute(query, (userid, )).fetchone()
    
    cartid = None
    if not row:
        newCartId = UUID()
        query = "insert into cart (cartid, cartholder) values (?, ?)"
        cursor.execute(query, (newCartId, userid))
        cartid = newCartId
    else: 
        cartid = row[0]

    query = 'insert into CartDetail (cartid, productid, sizeid, quantity) values (?, ?, ?, ?)'

    if len(data) != 0:
        cursor.executemany(query, tuple((cartid, item['productid'], item['size'], item['quantity']) for item in data))
        cursor.commit()
        threading.Thread(target=__updateTotalPriceBG, args = (cartid, ), daemon = True).start()
    
    return { "message": "saved!" }

def ProcessGetMyCart(userid):
    query = "select cartid, totalprice  from cart where cartholder = ? and opening = 1"
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (userid,)).fetchone()
    
    if not row:
        cartid = UUID()
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
    
def ProcessGetMySharedCart(userid):
    query = "select cartid, totalprice, numbersofmembers, createdat, cartname, totalitems from sharedcart where cartholder = ? and opening = 1"
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid,)).fetchall()
    return {
        "shared-carts": [
            {
                "id": row[0],
                "totalprice": row[1],
                "members": row[2],
                "createdat": datetime.datetime.strftime(row[3] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[3] else None,
                "cartname": row[4],
                "totalitems": row[5] if row[5] is not None else 0
            } for row in rows
        ]
    }

def ProcessGetGetMyJoinedCart(userid):
    query = ''' select cartid, cartholder, totalprice, numbersOfMembers, createdAt, FullName, u.avatar, cc.cartname, cc.totalitems  from (
        select sc.cartid, c.cartholder, c.opening, c.totalprice, c.numbersOfMembers, c.createdAt, cartname, totalitems
        from (select cartid from SharedCartMember where memberid = ?) as sc 
        join (select * from sharedcart where opening = 1) c on (sc.cartid = c.cartid and c.cartholder != ?)
    ) cc left join users u on (userid = cc.cartholder)
    '''
    
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid, userid, )).fetchall()
    
    if not __backgroundProcessing:
        __backgroundQueue.append(['select * from size', ( )])
    else: __backgroundQueueTMP.append(['select * from size', ( )])
    
    return {
        "joined-carts": [
            {
                "id": row[0],
                "cartholder": row[1],
                "totalprice": row[2],
                "members": row[3],
                "createdat": datetime.datetime.strftime(row[4] + datetime.timedelta(hours = 7), '%d-%m-%Y') if row[4] else None,
                "cartholder": row[5],
                "cartholderavatar": row[6],
                "cartname": row[7],
                "totalitems": row[8]
            } for row in rows
        ]
    }
    
def PersonalSharedListInfo(userid):
    query = '''select sc.cartid, cartholder, memberid, cartname from sharedcart sc join (select * from SharedCartMember where memberid = ?) scm on ( 
        sc.cartid = scm.cartid
    ) where opening = 1'''
    
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query, (userid, )).fetchall()
    
    return {
        "shared": [
            {
                "cartid": row[0],
                "cartname": row[3],
            }
            for row in rows if row[1] == row[2]  
        ],
        "joined": [
            {
                "cartid": row[0],
                "cartname": row[3],
            }
            for row in rows if row[2] != row[1]
        ]
    }
    
def ProcessGetSharedCartInfo(cartid, userid):
    query = "select * from sharedcartmember where cartid = ? and memberid = ?"
    cursor = Connector.establishConnection().cursor()
    row = cursor.execute(query, (cartid, userid, )).fetchone()

    if not row:
        raise Exception("Cart not found")
    
    query = '''
        select cartid, productid, sizeid, COALESCE(sum(quantity), 0), Title, Descriptions, Price, SexId, CategoryId, ImageUrl
        from (select * from SharedCartDetails where cartid = ?) scm join Product p 
        on ( p.id = scm.productid )
        group by cartid, productid, sizeid, Title, Descriptions, Price, SexId, CategoryId, ImageUrl
    '''
    
    rows = cursor.execute(query, (cartid, )).fetchall()
    
    res = { }
    
    res["items"] = [
        {
            "sizeid": row[2],
            "quantity": row[3],
            "product": {
                "id": row[1],
                "title": row[4],
                "description": row[5],
                "price": row[6],
                "sex": row[7],
                "category": row[8],
                "images": [row[9]]
            }
        } for row in rows
    ]
    
    res["info"] = __summaryOfSharedInfo(cartid)
    
    res['info']['totalprice'] = sum(row[3] * priceMapping[row[1]] for row in rows)
    res['info']['totalitems'] = sum(row[3] for row in rows)
    
    res["logs"] = [
        line[0] for line in cursor.execute('select note from SharedCartHistory where cartid = ?', (cartid, )).fetchall()
    ]
    
    return res