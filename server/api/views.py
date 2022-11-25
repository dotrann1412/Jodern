from django.shortcuts import render

# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from  rest_framework.request import Request
import numpy as np

from .data import *

from modules.retriever import (
    ImageRetriever,
    TextRetriever
)

class Test(APIView):
    def get(self, request, *args, **kwargs):
        return Response({"message", "Hello world!"}, status = status.HTTP_200_OK)
    
    def post(self, request, *args, **kwargs):
        return Response({"message", "Hello world!"}, status = status.HTTP_200_OK)

textRetriever = TextRetriever.TextRetriever()
imageRetriever = ImageRetriever.ImageRetriever()

class SearchEngineInterface(APIView):
    def get(self, request, *args, **kwargs):
        query = request.query_params.get('query', '').lower()
        searchType = request.query_params.get('type', '').lower()
        
        if searchType == 'text': # process searching for image
            return Response(GetProductsByList(textRetriever.search(query)), status = status.HTTP_200_OK)
        elif searchType == 'image':
            if query == '':
                query = np.array(request.data.decode('utf-8'))
            return Response(GetProductsByList(imageRetriever.search(query)), status = status.HTTP_200_OK)
            
        return Response({"message": "Searching type not found"}, status = status.HTTP_501_NOT_IMPLEMENTED)

class HandleProductsList(APIView):
    def get(self, request: Request, *args, **kwargs):
        sex, category = None, None
        
        sex = request.query_params.get('sex', None)
        category = request.query_params.get('category', None)
                
        res = GetProducts(sex, category)
        if not res:
            return Response({}, status = status.HTTP_400_BAD_REQUEST)

        return Response(res, status = status.HTTP_200_OK)

class HandleProductsByID(APIView):
    def get(self, request: Request, *args, **kwargs):
        return Response(GetProductDetails(kwargs['id']), status = status.HTTP_200_OK)

class HandleCategoriesTree(APIView):
    def get(self, request, *args, **kwargs):
        return Response(GetCategoriesTree(), status = status.HTTP_200_OK)

class StoresLocation(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"message": "Searching type not found"}, status = status.HTTP_200_OK)