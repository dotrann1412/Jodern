from django.urls import path, include

from .views import (
    Test
)

urlpatterns = [ 
    path('test/', Test.as_view()),
]