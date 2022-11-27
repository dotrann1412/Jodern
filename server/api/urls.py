from django.urls import path, include

from .views import (
    Test, HandleProductsList, HandleProductsByID, SearchEngineInterface, HandleCategoriesTree
)

urlpatterns = [ 
    path('test/', Test.as_view()),
    path('product-list/', HandleProductsList.as_view()),
    path('product/<str:id>/', HandleProductsByID.as_view()),
    path('search/', SearchEngineInterface.as_view()),
    path('submit-order/', Test.as_view()),
    path('validate-order/', Test.as_view()),
    path('categories/', HandleCategoriesTree.as_view())
]