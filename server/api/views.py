
# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from  rest_framework.request import Request
import numpy as np
from modules.authentication.authenticator import Authenticator, Verifier

import base64
import numpy as np

from .processor.products import GetProductsByList_2, GetProducts, GetProductDetails

from .processor.orders import (
    ProcessPersonalOrder, GetPersonalOrdersData, PersonalOrderDetails, ProcessSharedOrder,
    ProcessMarkAsDelivered
)

from .processor.misc import GetCategoriesTree, StoresLocationJson

from .processor.analysis import TrendingItems, HighlightItems, RelatedItems

from .processor.personalize import GetWishList, AddToUserWishList, RemoveFromUserWishList, UpdateUserWishList

from .processor.carts import (
    AddToCart, ProcessGetMyCart, GetSharedCartInfo, ProcessMakeSharedCart, ProcessSavePersonalCart,
    ProcessJoinSharedCart, ProcessGetMySharedCart, ProcessGetGetMyJoinedCart, ProcessGetSharedCartInfo,
    PersonalSharedListInfo
)

# from modules.retriever import (
#     ImageRetriever,
#     TextRetriever
# )

class Test(APIView):
    def get(self, request, *args, **kwargs):
        return Response({"message": "Hello world!"}, status = status.HTTP_200_OK)
    
    def post(self, request, *args, **kwargs):
        return Response({"message": "Hello world!"}, status = status.HTTP_200_OK)

# textRetriever = TextRetriever.TextRetriever()
# imageRetriever = ImageRetriever.ImageRetriever()
import traceback
class SearchEngineInterface(APIView):
    def get(self, request: Request, *args, **kwargs):
        query = request.query_params.get('query', '').lower()
        limit = request.query_params.get('limit', None)
        try:
            res = textRetriever.search(query) # = GetProductsByList_2(textRetriever.search(query))
            
            if limit is not None:
                limit = int(limit)
                res = res[: min(len(res), limit)]

            return Response(GetProductsByList_2(res), status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        
        return Response({"message": "Hjhj"}, status = status.HTTP_400_BAD_REQUEST)
    
    def post(self, request: Request, *args, **kwargs):
        try:
            query = base64.decodebytes(request.data['query'].encode())
            limit = request.data.get('limit', None)
            query = np.frombuffer(query, dtype = np.uint8)
            query = np.reshape(query, (256, 256, 3))
            res = imageRetriever.search(query)

            if limit is not None:
                limit = int(limit)

                res = res[: min(len(res), limit)]
            return Response(GetProductsByList_2(res), status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        return Response({"message": "Method not implemented"}, status = status.HTTP_400_BAD_REQUEST)

class HandleProductsList(APIView):
    def get(self, request: Request, *args, **kwargs):
        sex, category = None, None
        sex = request.query_params.get('sex', None)
        category = request.query_params.get('category', None)
        pageIndex = request.query_params.get('page', None)
        
        if pageIndex is not None:
            pageIndex = int(pageIndex)
        
        res = {}
    
        try:
            if 'id' in request.query_params:
                ids = request.query_params['id'].split(',')
                res = GetProductsByList_2(ids, pageIndex)
            else:
                res = GetProducts(sex, category, pageIndex)
        except Exception as err:    
            print('[EXCEPTION] Details here: ', err)
            traceback.print_exc()
            return Response({'Message': "Wrong request format"}, status = status.HTTP_400_BAD_REQUEST)

        return Response(res, status = status.HTTP_200_OK)
    
class Checkout(APIView):
    def post(self, request: Request, *arg, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if token is None or 'userid' not in token:
                return Response({'message': 'invalid token'}, status = status.HTTP_401_UNAUTHORIZED)
            
            userid = token['userid']
            
            if 'cartid' not in request.data or request.data['cartid'] == '*': res = ProcessPersonalOrder(userid, request.data.get('order-info', {}))
            else: res = ProcessSharedOrder(userid, request.data['cartid'], request.data.get('order-info', {}))
        
        except Exception as e:
            traceback.print_exc()
            return Response({'message': e}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)

        return Response(res, status = status.HTTP_200_OK)

class HandleProductsByID(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid = None    
        try: 
            token = Verifier.decode(request.headers['access-token'])
            userid = token['userid']
        except: pass
        
        try: res = GetProductDetails(kwargs['id'], userid)
        except Exception as err:
            traceback.print_exc()
            res = None

        if not res or any(key in res for key in ['error', 'errors']):
            return Response({'message': 'item not found'}, status = status.HTTP_400_BAD_REQUEST)
        
        return Response(res, status = status.HTTP_200_OK)

class HandleCategoriesTree(APIView):
    def get(self, request, *args, **kwargs):
        try:
            tree = GetCategoriesTree()
        except:
            traceback.print_exc()
            return Response(tree, status = status.HTTP_200_OK)
            
        return Response(tree, status = status.HTTP_200_OK)

class StoresLocation(APIView):
    def get(self, request, *args, **kwargs):
        return Response(StoresLocationJson(), status = status.HTTP_200_OK)

class Trending(APIView):
    def get(self, request, *args, **kwargs):
        try: res = TrendingItems(kwargs['top_k'])
        except: traceback.print_exc()
        return Response(TrendingItems(kwargs['top_k']), status = status.HTTP_200_OK)

class Highlight(APIView):
    def get(self, request, *args, **kwargs):
        return Response(HighlightItems(kwargs['top_k']), status = status.HTTP_200_OK)
    
class RelatedProduct(APIView):
    def get(self, request, *args, **kwargs):
        try:
            id = request.query_params.get('id', None)
            top_k = int(request.query_params.get('top_k', 0), 10)
            res = RelatedItems(id, top_k)
        except Exception as err:
            traceback.print_exc()
            print('[EXCEPTION] Processing error. Details here: ', err)
            return Response({'message': 'Processing error'}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)
        return Response(res, status = status.HTTP_200_OK)

class Login(APIView):
    def post(self, request: Request, *args, **kwargs):
        res = {}
        try:
            res = Authenticator.Login(**request.data)
        except Exception as err:
            traceback.print_exc() 
            res = {'error': "Error occured while processing request"}
            
        return Response(res, status = status.HTTP_200_OK if 'error' not in res else status.HTTP_401_UNAUTHORIZED)

class UserProfile(APIView):
    def get(self, request: Request, *args, **kwargs):
        res = {}
        try:
            res = Authenticator.GetUser(request.query_params.get('token', ''))
        except Exception as err:
            traceback.print_exc() 
            res = {'error': "Error occured while processing request"}
            
        return Response(res, status = status.HTTP_200_OK if 'error' not in res else status.HTTP_401_UNAUTHORIZED)
    
    def post(self, request: Request, *args, **kwargs):
        res = {}
        try:
            res = Authenticator.UpdateUser(request.data)
        except Exception as err:
            traceback.print_exc() 
            res = {'error': "Error occured while processing request"}
            
        return Response(res, status = status.HTTP_200_OK if 'error' not in res else status.HTTP_401_UNAUTHORIZED)

class FetchWishList(APIView):
    def get(self, request: Request, *args, **kwargs):
        res = None
        
        try:
            token = Verifier.decode(request.headers['access-token'])
            
            if 'userid' not in token:
                raise Exception('Invalid access token')
            
            pageIndex = request.query_params.get('page', None)
            
            if pageIndex is not None:
                pageIndex = int(pageIndex)

            res = GetWishList(token['userid'], pageIndex)
            
        except Exception as err:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        
        return Response(res, status = status.HTTP_200_OK)

class UpdateWishList(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        
        try:
            token = Verifier.decode(request.headers['access-token'])
            
            if 'userid' not in token:
                raise Exception('Invalid access token')
            
            userid = token['userid']
        except Exception as err:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        try:
            res = UpdateUserWishList(userid, request.data.get('wishlist', []))
        except:
            traceback.print_exc()

        return Response(res, status = status.HTTP_200_OK)

class AddToWishlist(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        
        try:
            token = Verifier.decode(request.headers['access-token'])
            
            if 'userid' not in token:
                raise Exception('Invalid access token')
            
            userid = token['userid']
        except Exception as err:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        res = AddToUserWishList(userid, request.data['product_id'])
        return Response(res, status = status.HTTP_200_OK)
    
class RemoveFromWishList(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        
        try:
            token = Verifier.decode(request.headers['access-token'])
            
            if 'userid' not in token:
                raise Exception('Invalid access token')
            
            userid = token['userid']
        except Exception as err:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        res = RemoveFromUserWishList(userid, request.data['product_id'])
        return Response(res, status = status.HTTP_200_OK)           

class FetchSharedCarts(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = ProcessGetMySharedCart(userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)
    
class FetchJoinedCarts(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = ProcessGetGetMyJoinedCart(userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)

class PersonalCart(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = ProcessGetMyCart(userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)

class AddItemToCart(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            
            if any(key not in request.data for key in ['productid', 'sizeid', 'quantity', 'cartids']):
                raise Exception('Invalid request')
            
            userid = token['userid']
            res = AddToCart(userid, request.data['productid'], request.data['sizeid'], request.data['quantity'], request.data['cartids'])
        except Exception as err:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)

class SharedCartAPI(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')

            userid = token['userid']
            cartid = request.query_params.get('cartid', None)
            res = GetSharedCartInfo(userid, cartid)

        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)
    
class MakeSharedCart(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            sharedCartName = request.data.get('name', 'New shared cart')
            res = ProcessMakeSharedCart(userid, sharedCartName)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)

class JoinSharedCart(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            sharedCartId = request.data.get('cartid', None)
            res = ProcessJoinSharedCart(sharedCartId, userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)
    
class SharedCartInfo(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            sharedCartId = request.data.get('cartid', None)
            res = ProcessGetSharedCartInfo(sharedCartId, userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)

class SharedSummary(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = PersonalSharedListInfo(userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)

class SaveCart(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = ProcessSavePersonalCart(userid, request.data['data'])
        except Exception as err:
            traceback.print_exc()
            return Response({'error': err}, status = status.HTTP_401_UNAUTHORIZED)
    
        return Response(res, status = status.HTTP_200_OK)
    
class MarkAsDelivered(APIView):
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')

            if 'orderid' not in request.data:
                raise Exception('Invalid order id')

            userid = token['userid']
            res = ProcessMarkAsDelivered(request.data['orderid'])

        except Exception as err:
            traceback.print_exc()
            return Response({'error': err}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)
    
class GetOrder(APIView):
    def get(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = GetPersonalOrdersData(userid)
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)
    
    def post(self, request: Request, *args, **kwargs):
        userid, res = None, None
        try:
            token = Verifier.decode(request.headers['access-token'])
            if 'userid' not in token:
                raise Exception('Invalid access token')
            userid = token['userid']
            res = PersonalOrderDetails(userid, request.data['orderid'])
        except:
            traceback.print_exc()
            return Response({'error': 'Invalid access token'}, status = status.HTTP_401_UNAUTHORIZED)

        return Response(res, status = status.HTTP_200_OK)