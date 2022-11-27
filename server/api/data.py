import os, json, requests


__productDataPath = 'data/appdata/products.json'
__ordersDataPath = 'data/appdata/orders.json'
__configFile = 'data/appdata/config.json'

__storesLocationPath = './data/appdata/stores-location.json'


with open(__storesLocationPath, 'r') as fp:
    __location = json.load(fp)['location']

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

from modules.email_service import gmail

__mailInstance = gmail.MailService()

try: __mailInstance.login('joderm.store@gmail.com', 'isowkkrraoqqihqk')
except: __mailInstance = None

import time

def ProcessOrder(order):
    response = {}

    while __commit.lock:
        time.sleep(0.1)
    
    for id, val in order['items'].items():
        for size, num in val.items():
            __drop(id, size, num)

    if __mailInstance:
        mailContent = gmail.build_email_content('joderm.store@gmail.com', order['info']['email'], subject = '', content = '')
        __mailInstance.send_mail(mailContent)
    
    __commit()

    return True

def ValidateOrder(order):
    if len(list(order.keys())) == 0:
        return {"message": "Empty cart!" , 'status': 'nOK'}
    
    issues = {
        
    }
    
    has_issues = False
    
    for id, val in order.items():
        for size, num in val.items():
            if __products1Layer[id]['inventory'][size] < num:
                issues[size] = __products1Layer[id]['inventory'][size]
                has_issues = True
    
    response = {
        "status": "OK" if not has_issues else "nOK"
    }
    
    if has_issues:
        response['issues'] = issues
    
    return response

def IsAbleForOrdering(id, num):
    if type(id) != str: id = str(id)
    return __products1Layer[id]['inventory'] >= num

def LocationList():
    return {'location': __location}

# update data
def __commit():
    __commit.lock = True
    try:
        with open(__productDataPath, 'wb',  encoding='utf-8') as fp:
            json.dump(__products, fp, indent = 4, ensure_ascii=False)
    except Exception as err:
        print(f'[STATUS] Exception rasied while pushing data to storage. Detail here: \n{err}')
    __commit.lock = False

__commit.lock = False

def __drop(id: str, size: str, num: int):
    sex, cat = __products1Layer['sex'], __products1Layer['category']
    __products1Layer[id]['inventory'][size] = max(0, __products1Layer[id]['inventory'][size] - num)
    __products[sex][cat]['inventory'][size] = max(0, __products1Layer[id]['inventory'][size] - num)
