import os, json, requests


__productDataPath = './data/appdata/products.json'
__ordersDataPath = './data/appdata/orders.json'

class Handler:
    with open(__productDataPath, 'r') as fp:
        __products = json.load(fp)
        pass
    
    __categories = {}
    __products1Layer = {}
    
    for sex, subcategories in __products.items():
        __categories[sex] = list(subcategories.keys())
        for key, value in subcategories:
            for item in value:
                __products1Layer[str(item["id"])] = item
    
    def GetProducts(sex, category):
        if not sex and not category:
            return []
        
        res = {}
        
        if not sex:
            sexList = list(Handler.__categories.keys())
            for sex in sexList:
                if category in Handler.__categories[sex]:
                    res[sex][category] = Handler.__products[sex][category]
            
    
        elif not category:
            res = {
                sex: Handler.__products[sex]
            }
        
        return res
    
    def GetCategoriesTree():
        return Handler.__categories
    
    def GetProductDetails(id):
        if type(id) != str:
            id = str(id)

        if id not in Handler.__products1Layer:
            return {}

        return Handler.__products1Layer[id]
    
    def GetStoreLocationList():
        pass
    
    def ProcessOrder(items):
        # return result of sending mail
        return {}
    
    def IsAbleForOrdering(id, num):
        if type(id) != str: id = str(id)
        return Handler.__products1Layer[id]['inventory'] >= num
    
    # update data
    def __commit():
        pass
    
    def __drop(id, num):
        pass