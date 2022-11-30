from django.shortcuts import render

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
            return Response({'data' : GetProductsByList(ids)}, status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        
        return Response({"message": "Hjhj"}, status = status.HTTP_400_BAD_REQUEST)
    
    def post(self, request: Request, *args, **kwargs):
        query = request.POST.get('query', '').lower()
        try:
            query = base64.decodebytes(query.encode('utf-8'))
            query = np.frombuffer(query, dtype = np.uint8)
            
            return Response({'data' : GetProductsByList(imageRetriever.search(query))}, status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        return Response({"message": "Method not implemented"}, status = status.HTTP_400_BAD_REQUEST)

class HandleProductsList(APIView):
    def get(self, request: Request, *args, **kwargs):
        sex, category = None, None
        sex = request.query_params.get('sex', None)
        category = request.query_params.get('category', None)

        try:
            res = GetProducts(sex, category)
        except Exception as err:
            print('[EXCEPTION] Details here: ', err)
            return Response({'Message': "Wrong request format"}, status = status.HTTP_400_BAD_REQUEST)

        return Response(res, status = status.HTTP_200_OK)
    
class ProcessOrder(APIView):
    def post(self, request: Request, *arg, **kwargs):
        try:    
            data = request.POST.get('cart', r'{}')
            data = json.loads(data)
            res = ProcessOrderData(data)
        except Exception as err:
            traceback.print_exc()            
            return Response({'message': 'wrong data format'}, status = status.HTTP_400_BAD_REQUEST) 
        
        return Response(res, status = status.HTTP_200_OK)

class ValidateOrder(APIView):
    def post(self, request: Request, *arg, **kwargs):
        try:    
            data = request.POST.get('cart', r'{}')
            print('[DEBUG] ', request.POST)
            data = json.loads(data)
            print('[DEBUG] ', data)
            res = ValidateOrderData(data)
        except Exception as err:
            print('[EXCEPTION] exception raised while validate order. Details here: ', err)
            return Response({'message': 'wrong data format'}, status = status.HTTP_400_BAD_REQUEST) 
        
        return Response(res, status = status.HTTP_200_OK)

class HandleProductsByID(APIView):
    def get(self, request: Request, *args, **kwargs):
        res = GetProductDetails(kwargs['id'])
        if 'id' not in res:
            return Response({'message': 'item not found'}, status = status.HTTP_400_BAD_REQUEST)
        return Response(res, status = status.HTTP_200_OK)

class HandleCategoriesTree(APIView):
    def get(self, request, *args, **kwargs):
        return Response(GetCategoriesTree(), status = status.HTTP_200_OK)

class StoresLocation(APIView):
    def get(self, request, *args, **kwargs):
        return Response(StoresLocationJson(), status = status.HTTP_200_OK)

class Trending(APIView):
    def get(self, request, *args, **kwargs):
        return Response({'data': TrendingItems(kwargs['top_k'])}, status = status.HTTP_200_OK)

class Highlight(APIView):
    def get(self, request, *args, **kwargs):
        try:
            res = {'data': HighlightItems(kwargs['top_k'])}
        except Exception as err:
            print('[EXCEPTION] Processing error. Details here: ', err)
            return Response({'message': 'Processing error'}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)
        return Response(res, status = status.HTTP_200_OK)
    
    
class RelatedProduct(APIView):
    def get(self, request, *args, **kwargs):
        try:
            id = request.query_params.get('id', None)
            top_k = request.query_params.get('top_k', None)
            res = {'data': RelatedItems(id, top_k)}
        except Exception as err:
            traceback.print_exc()
            print('[EXCEPTION] Processing error. Details here: ', err)
            return Response({'message': 'Processing error'}, status = status.HTTP_500_INTERNAL_SERVER_ERROR)
        return Response(res, status = status.HTTP_200_OK)
