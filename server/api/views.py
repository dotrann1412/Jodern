from django.shortcuts import render

# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from .data import Handler

from modules.retriever import (
    ImageRetriever,
    TextRetriever
)

class Test(APIView):
    def get(self, request, *args, **kwargs):
        return Response({"message", "Hello world!"}, status = status.HTTP_200_OK)
    
    def post(self, request, *args, **kwargs):
        return Response({"message", "Hello world!"}, status = status.HTTP_200_OK)

class SearchEngineInterface(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        
        searchType = data.get("type")
        query = data.get("query")
        
        if searchType == 'TEXT': # process searching for image
            retval = {}
            return Response(retval, status = status.HTTP_200_OK)
        elif searchType == 'IMAGE':
            retval = {}
            return Response(retval, status = status.HTTP_200_OK)

        return Response({"message": "Searching type not found"}, status = status.HTTP_501_NOT_IMPLEMENTED)

class HandleProductsList(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"message": "Searching type not found"}, status = status.HTTP_200_OK)

class HandleProductsByID(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"message": "Searching type not found"}, status = status.HTTP_200_OK)

class HandleProductsByCategory(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"message": "Searching type not found"}, status = status.HTTP_200_OK)

class HandleCategoriesTree(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"message": "Searching type not found"}, status = status.HTTP_200_OK)

class StoresLocation(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"message": "Searching type not found"}, status = status.HTTP_200_OK)