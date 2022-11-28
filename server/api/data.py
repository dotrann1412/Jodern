import os, json, requests
import threading

__productDataPath = 'data/appdata/products.json'
__ordersDataPath = 'data/appdata/orders.json'
__configFile = 'data/appdata/config.json'

__storesLocationPath = './data/appdata/stores-location.json'


with open(__storesLocationPath, 'r') as fp:
    __location = json.load(fp)['location']

with open(__productDataPath, 'rb') as fp:
    __products = json.loads(fp.read().decode('utf-8'))

with open(__ordersDataPath, 'r') as fp:
    __orderedItems = json.load(fp)
    
__categories = {}
__products1Layer = {}

for sex, subcategories in __products.items():
    __categories[sex] = list(subcategories.keys())
    for key, value in subcategories.items():
        for item in value:
            __products1Layer[str(item["id"])] = item
            
import copy
__productsLightWeight = copy.deepcopy(__products)

for sex, cat in __categories.items():
    for c in cat:
        for iter in range(len(__productsLightWeight[sex][c])):
            __productsLightWeight[sex][c][iter]['images'] = __productsLightWeight[sex][c][iter]['images'][:1]

print('[STATUS] Data loaded')

def GetProductsByList(l: list):
    return [__products1Layer[str(i)] for i in l]

def GetProducts(sex, category):
    if not sex and not category:
        return None
    
    if sex and category:
        return __productsLightWeight.get(sex, {}).get(category, None)
    
    if sex and sex in __categories.keys():
        return __productsLightWeight[sex]
    
    res = {}
    
    for sex, value in __productsLightWeight.items():
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

try: __mailInstance.login('jodern.store@gmail.com', 'isowkkrraoqqihqk')
except: __mailInstance = None

import time

def ProcessOrder(order):
    response = {}

    while __commit.lock:
        time.sleep(0.1)
    
    for id, val in order['items'].items():
        for size, num in val.items():
            __drop(id, size, num)
            __orderedItems[id] += num

    if __mailInstance:
        mailContent = gmail.build_email_content('jodern.store@gmail.com', order['info']['email'], subject = '', content = '')
        __mailInstance.send_mail(mailContent)
    
    __commit()

    return True

def TrendingItems(top_k = 5):
    items = [{'id': key, 'count': val} for key, val in __orderedItems.items()]
    items.sort(key = lambda item: item['count'], reverse = True)
    return items[:top_k]

def HighlightItems(top_k = 5):
    items = [
        {
            'id': id, 
            'title': val['title'],
            'inventory': val['inventory'],
            'price': val['price'],
            'sex': val['sex'],
            'category': val['category'],
            'images': [val['images'][0]]
        } for id, val in __products1Layer.items()
    ]
    items.sort(key = lambda item: sum([val for _, val in item['inventory'].items()]))
    return items[:top_k]

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

def IsAbleForOrdering(id, size, num):
    if type(id) != str: id = str(id)
    return __products1Layer[id]['inventory'][size] >= num

def StoresLocationJson():
    return {'location': __location}

# update data
def __commit(fielToPush = __productDataPath):
    __commit.lock = True
    try:
        with open(__productDataPath, 'w') as fp:
            json.dump(__products, fp, indent = 4, ensure_ascii = False)

        with open(__ordersDataPath, 'w') as fp:
            json.dump(__orderedItems, fp, indent = 4, ensure_ascii = False)

    except Exception as err:
        print(f'[STATUS] Exception rasied while pushing data to storage. Detail here: \n{err}')
    __commit.lock = False

__commit.lock = False

def __drop(id: str, size: str, num: int):
    if type(id) != str: id = str(id)
    sex, cat = __products1Layer[id]['sex'], __products1Layer[id]['category']
    
    pos = -1
    for iter, item in enumerate(__products[sex][cat], 0):
        if item['id'] == int(id):
            pos = iter
            break
        
    if pos != -1:
        __products[sex][cat][pos]['inventory'][size] = max(0, __products1Layer[id]['inventory'][size] - num)
        __productsLightWeight[sex][cat][pos]['inventory'][size] = max(0, __products1Layer[id]['inventory'][size] - num)
    
    __products1Layer[id]['inventory'][size] = max(0, __products1Layer[id]['inventory'][size] - num)

def __sync():
    while True:
        while __commit.lock:
            time.sleep(0.1)
        __commit()
        time.sleep(5)

import random
def __cloneUser():
    while True:
        c = GetCategoriesTree()
        sex = random.choice(list(c.keys()))
        cat = random.choice(c[sex])
        
        items = __products[sex][cat]

        for item in items:
            while __commit.lock:
                time.sleep(0.1)

            num = random.randint(-3, 4)
            size = random.choice(['S', 'M', 'L', 'XL', 'XXL'])
            
            if num < 0:
                if item['id'] in __orderedItems:
                    __orderedItems[item['id']] -= num
                else:  __orderedItems[item['id']] = -num
            
            __drop(item['id'], size, num)
        
        time.sleep(60)
        

threading.Thread(target = __sync, args = (), daemon = True).start()
print('[STATUS] sync thread start')
threading.Thread(target = __cloneUser, args = () , daemon = True).start()
print('[STATUS] clone user thread start')