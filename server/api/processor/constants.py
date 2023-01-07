from modules.helpers import config
from modules.data_acess.driver import Connector

asciiBase = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
ITEM_PER_PAGE = config.Config.getValue('item-per-page')
categoryName = None

if not categoryName:
    query = 'select * from category'
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    categoryName = {row[0]: row[1] for row in rows}

sexName = None

if not sexName:
    query = 'select * from Sex'
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    sexName = {row[0]: row[1] for row in rows}

allIdsList = None
if not allIdsList:
    query = "select id from product"
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    allIdsList = [row[0] for row in rows]
    
storeDict = None
if not storeDict:
    query = 'select locationid, X_coordinate, Y_coordinate, locationname, address from Location'
    cursor = Connector.establishConnection().cursor()
    rows = cursor.execute(query).fetchall()
    storeDict = {
        'branchs': [{
                'branch_id': row[0],
                'branch_name': row[3],
                'address': row[4],
                'coordinate': [row[1], row[2]]
        } for row in rows] 
    }
    
categories = None
if not categories:
    query = "select sexid, categoryid from product group by sexid, categoryid"
    rows = Connector.establishConnection().cursor().execute(query).fetchall()
    
    categories = {}
    for row in rows:
        if row[0] not in categories: categories[row[0]] = []
        categories[row[0]] += [row[1]]