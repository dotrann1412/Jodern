from modules.data_acess.driver import Connector
from .products import GetProductsByList_2

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
        return { "message": f"{productid} is already in user's wishlist!" }
    
    return { "message": "Done!" }

def RemoveFromUserWishList(userid, productid):
    query = 'delete from wishlist where userid = ? and productid = ?'
    cursor = Connector.establishConnection().cursor()
    
    try:
        cursor.execute(query, (userid, productid))
        cursor.commit()
    except:
        return { "message": f"{productid} has not added to user's wishlist!" }
    
    return { "message": "Removed!" }
    
def UpdateUserWishList(userid, wishlist):
    query = 'delete from wishlist where userid = ?'
    cursor = Connector.establishConnection().cursor()
    cursor.execute(query, (userid, ))
    cursor.commit()
    query = 'insert into wishlist (userid, productid) values (?, ?)'
    cursor.executemany(query, tuple((userid, item) for item in wishlist))
    cursor.commit()
    
    return {
        'message': "All done!"
    }