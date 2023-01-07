from modules.data_acess.driver import Connector
import threading, time, datetime


__pool = {
    
}

__locked = False
__cachingTime = 60 * 30

def __summaryOfSharedInfo(cartid):
    query = '''
        select sc.cartid, sc.cartholder, sc.numbersOfMembers, sc.totalprice, sc.createdAt, Users.FullName as holdername, users.avatar as holderavatar, cartname, totalitems
        from (select * from SharedCart where cartid  = ?) sc
        join users on (
            userid = sc.cartholder
        )
    '''
    
    cursor = Connector.establishConnection().cursor()
    generalInfoRow = cursor.execute(query, (cartid, )).fetchone()
    
    query = '''
        select cartid, memberid, productid, sizeid, quantity from SharedCartDetails where cartid = ?
    '''
    
    itemsInfoRows = cursor.execute(query, (cartid, )).fetchall()
    
    return {
        "id": generalInfoRow[0],
        "cartholder": {
            "id": generalInfoRow[1],
            "name": generalInfoRow[5],
            "avatar": generalInfoRow[6]
        },
        "numbersOfMembers": generalInfoRow[2],
        "totalprice": generalInfoRow[3],
        "createdAt": datetime.datetime.strftime(generalInfoRow[4] + datetime.timedelta(hours = 7), '%d-%m-%Y') if generalInfoRow[4] else None,
        "cartname": generalInfoRow[7],
        "totalitems": generalInfoRow[8],
        "items": {
            f"{row[2]}": {
                "sizeid": row[3],
                "quantity": row[4],
            } for row in itemsInfoRows
        }
    }
    
def AddToSharedCart(cartid, userid, productid, sizeid, quantity):
    pass

def GetCart(cartid):
    global __pool
    if cartid not in __pool:
        __pool[cartid] = __summaryOfSharedInfo(cartid)
    __pool[cartid]['timeout'] = datetime.datetime.now() + __cachingTime
    return __pool[cartid]

def __bg(timeout = 60 * 60):
    removed = [key for key, val in __pool.items() if val['timeout'] < datetime.datetime.now().timestamp()]
    for key in removed:
        del __pool[key]
    threading.Timer(timeout, __bg).start()

__bg()