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

import base64
import numpy as np

from .data import *

from modules.retriever import (
    ImageRetriever,
    TextRetriever
)

class Test(APIView):
    def get(self, request, *args, **kwargs):
        print('[STATUS] GET api/test')
        return Response({"message": "Hello world!"}, status = status.HTTP_200_OK)
    
    def post(self, request, *args, **kwargs):
        print('[STATUS] POST api/test')
        return Response({"message": "Hello world!"}, status = status.HTTP_200_OK)

textRetriever = TextRetriever.TextRetriever()
# imageRetriever = ImageRetriever.ImageRetriever()
import traceback
class SearchEngineInterface(APIView):
    def get(self, request: Request, *args, **kwargs):
        query = request.query_params.get('query', '').lower()
        
        print(f'[STATUS] GET api/search/text')

        try:
            ids = textRetriever.search(query)
            return Response({'data' : GetProductsByList(ids)}, status = status.HTTP_200_OK)
        except Exception as err:
            traceback.print_exc()
            return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        
        return Response({"message": "Hjhj"}, status = status.HTTP_400_BAD_REQUEST)
    
    def post(self, request: Request, *args, **kwargs):
        query = request.POST.get('query', '').lower()
        print('POST request for search image triggered')
        # try:
        #     if searchType == 'image':
        #         query = np.frombuffer(query.encode('ascii'), np.float32)
        #         print("Searching...")
        #         return Response({'data' : GetProductsByList(imageRetriever.search(query))}, status = status.HTTP_200_OK)
        # except Exception as err:
        #     traceback.print_exc()
        #     return Response({"message": "Error on processing"}, status = status.HTTP_500_INTERNAL_SERVER_ERROR) 
        return Response({"message": "Method not implemented"}, status = status.HTTP_400_BAD_REQUEST)

class HandleProductsList(APIView):
    def get(self, request: Request, *args, **kwargs):
        sex, category = None, None
        print('[STATUS] GET api/product-list')
        sex = request.query_params.get('sex', None)
        category = request.query_params.get('category', None)

        res = GetProducts(sex, category)
        if not res:
            return Response({}, status = status.HTTP_400_BAD_REQUEST)

        return Response(res, status = status.HTTP_200_OK)
    
class HanldeOrder(APIView):
    def get(self, request: Request, *arg, **kwargs):
        pass

class HandleProductsByID(APIView):
    def get(self, request: Request, *args, **kwargs):
        print('[STATUS] GET api/product')
        return Response(GetProductDetails(kwargs['id']), status = status.HTTP_200_OK)

class HandleCategoriesTree(APIView):
    def get(self, request, *args, **kwargs):
        print('[STATUS] GET api/categories')
        return Response(GetCategoriesTree(), status = status.HTTP_200_OK)

class StoresLocation(APIView):
    def get(self, request, *args, **kwargs):
        print('[STATUS] GET api/stores-location')
        return Response(StoresLocationJson(), status = status.HTTP_200_OK)
