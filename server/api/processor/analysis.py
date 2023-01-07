from modules.data_acess.driver import Connector
from .products import GetProductsByList
import random

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