
# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from  rest_framework.request import Request
from django.views.generic import TemplateView
import numpy as np
from django.http import HttpResponse
from django.views.generic import View
from PIL import Image
import io

import base64
import numpy as np

from .data import *

from modules.retriever import (
    ImageRetriever,
    TextRetriever
)

class Test(APIView):
    def get(self, request, *args, **kwargs):
        return Response({"message": "Hello world!"}, status = status.HTTP_200_OK)
    
    def post(self, request, *args, **kwargs):
        return Response({"message": "Hello world!"}, status = status.HTTP_200_OK)

textRetriever = TextRetriever.TextRetriever()
imageRetriever = ImageRetriever.ImageRetriever()
import traceback
class SearchEngineInterface(APIView):
    def get(self, request: Request, *args, **kwargs):
        query = request.query_params.get('query', '').lower()
        try:
            ids = textRetriever.search(query)
            return Response(GetProductsByList([id + 1 for id in ids]), status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        
        return Response({"message": "Hjhj"}, status = status.HTTP_400_BAD_REQUEST)
    
    def post(self, request: Request, *args, **kwargs):
        try:
            query = base64.decodebytes(request.data['query'].encode())
            query = np.frombuffer(query, dtype = np.uint8)
            query = np.reshape(query, (256, 256, 3))
            return Response(GetProductsByList([id + 1 for id in imageRetriever.search(query)]), status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        return Response({"message": "Method not implemented"}, status = status.HTTP_400_BAD_REQUEST)

class HandleProductsList(APIView):
    def get(self, request: Request, *args, **kwargs):
        sex, category = None, None
        sex = request.query_params.get('sex', None)
        category = request.query_params.get('category', None)
    
        res = {}
    
        try:
            if 'id' in request.query_params:
                ids = request.query_params['id'].split(',')
                res = GetProductsByList(ids)
            else: res = GetProducts(sex, category)
        except Exception as err:    
            print('[EXCEPTION] Details here: ', err)
            traceback.print_exc()
            return Response({'Message': "Wrong request format"}, status = status.HTTP_400_BAD_REQUEST)

        return Response(res, status = status.HTTP_200_OK)
    
class ProcessOrder(APIView):
    def post(self, request: Request, *arg, **kwargs):
        try:    
            res = ProcessOrderData(request.data['cart'])
        except Exception as err:
            traceback.print_exc()
            return Response({'message': 'wrong data format'}, status = status.HTTP_400_BAD_REQUEST) 
        print('[STATUS] process order completed') 
        return Response(res, status = status.HTTP_200_OK)

class ValidateOrder(APIView):
    def post(self, request: Request, *arg, **kwargs):
        try:
            res = ValidateOrderData(request.data['cart'])
        except Exception as err:
            print('[EXCEPTION] exception raised while validate order. Details here: ', err)
            return Response({'message': 'wrong data format'}, status = status.HTTP_400_BAD_REQUEST) 
        
        return Response(res, status = status.HTTP_200_OK)

class HandleProductsByID(APIView):
    def get(self, request: Request, *args, **kwargs):
        try:
            res = GetProductDetails(kwargs['id'])
        except Exception as err:
            traceback.print_exc()
            
        if 'id' not in res:
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
            top_k = request.query_params.get('top_k', None)
            res = RelatedItems(id, top_k)
        except Exception as err:
            traceback.print_exc()
            print('[EXCEPTION] Processing error. Details here: ', err)
            return Response({'message': 'Processing error'}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)
        return Response(res, status = status.HTTP_200_OK)
