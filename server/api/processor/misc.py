from .constants import (
    categoryName, sexName, categories, storeDict, asciiBase
)

import random

def GetCategoryName(code):
    global categoryName
    return categoryName[code]

def GetSexName(code):
    global sexName
    return sexName[code]

def GetCategoriesTree():
    global categories
    return categories

def StoresLocationJson():
    global storeDict
    return storeDict

def UUID(length = 10):
    global asciiBase
    return ''.join([asciiBase[random.randint(0, len(asciiBase) - 1)] for _ in range(length)])

def BranchInfo(branchid):
    global storeDict
    for store in storeDict['branchs']:
        if store['branch_id'] == branchid:
            return store
    return storeDict['branchs'][0]