from django.shortcuts import render

# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from modules.retriever import (
    ImageRetriever,
    TextRetriever
)

class Test(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)

class Search(APIView):
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)

class GetProductsList(APIView):
    '''
        Args:
            list of categories
        Returns:
            - OK: Res({}, 200)
            - nOK: Res({}, 400)
    '''
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)

class GetProducts(APIView):
    '''
        Args:
            prod id
        Returns:
            - OK: Res({}, 200)
            - nOK: Res({}, 400)
    '''
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)

class CartInfo(APIView):
    '''
        Args:
            prod id
        Returns:
            - OK: Res({}, 200)
            - nOK: Res({}, 400)
    '''
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)

class UpdateCart(APIView):
    '''
        Args:
            prod id
        Returns:
            - OK: Res({}, 200)
            - nOK: Res({}, 400)
    '''
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)

class StoresLocation(APIView):
    '''
        Args:
            prod id
        Returns:
            - OK: Res({}, 200)
            - nOK: Res({}, 400)
    '''
    def get(self, request, *args, **kwargs):
        data = request.data
        return Response({"hello", "world"}, status = status.HTTP_200_OK)