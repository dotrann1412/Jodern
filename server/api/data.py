import os, json, requests


__productDataPath = 'data/appdata/products.json'
__ordersDataPath = 'data/appdata/orders.json'

with open(__productDataPath, 'rb') as fp:
    __products = json.loads(fp.read().decode('utf-8'))

__categories = {}
__products1Layer = {}

for sex, subcategories in __products.items():
    __categories[sex] = list(subcategories.keys())
    for key, value in subcategories.items():
        for item in value:
            __products1Layer[str(item["id"])] = item

print('[STATUS] Data loaded')

def GetProductsByList(l: list):
    return [__products1Layer[str(i)] for i in l]

def GetProducts(sex, category):
    if not sex and not category:
        return None
    
    if sex and category:
        return __products.get(sex, {}).get(category, None)
    
    if sex and sex in __categories.keys():
        return __products[sex]
    
    res = {}
    
    for sex, value in __products.items():
        if category in value:
            res[sex] = value[category]            
                
    return res

def GetCategoriesTree():
    return __categories

def GetProductDetails(id):
    if type(id) != str:
        id = str(id)

    if id not in __products1Layer:
        return {}

    return __products1Layer[id]

def GetStoreLocationList():
    pass

def ProcessOrder(items):
    # return result of sending mail
    return {}

def IsAbleForOrdering(id, num):
    if type(id) != str: id = str(id)
    return __products1Layer[id]['inventory'] >= num

# update data
def __commit():
    pass

def __drop(id, num):
    pass