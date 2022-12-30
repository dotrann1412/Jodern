from django.urls import path, include

from .views import (
    Test, HandleProductsList, HandleProductsByID, 
    SearchEngineInterface, HandleCategoriesTree, StoresLocation,
    Trending, Highlight, ProcessOrder, RelatedProduct,
    Login, ValidateAccessToken, AddToWishlist, RemoveFromWishList,
    FetchWishList, UpdateWishList, FetchCart, AddItemToCart, SaveCart,
    GetOrder
)

urlpatterns = [ 
    path('test/', Test.as_view())
]

urlpatterns += [
    path('product-list/', HandleProductsList.as_view()),
    path('product/<str:id>/', HandleProductsByID.as_view())
]

urlpatterns += [
    path('search/', SearchEngineInterface.as_view())
]

urlpatterns += [
    path('categories/', HandleCategoriesTree.as_view()),
    path('stores-location/', StoresLocation.as_view()),
]
 
urlpatterns += [
    path('trending/<int:top_k>', Trending.as_view()),
    path('best-selling/<int:top_k>', Trending.as_view()),
    path('highlight/<int:top_k>', Highlight.as_view()),
    path('related/', RelatedProduct.as_view()),
]

urlpatterns += [
    path('login/', Login.as_view()),
    path('profile/', Test.as_view()),
    path('wishlist/', FetchWishList.as_view()),
    path('verify-token', ValidateAccessToken.as_view()),
]

urlpatterns += [
    path('add-to-wishlist/', AddToWishlist.as_view()),
    path('remove-from-wishlist/', RemoveFromWishList.as_view()),
    path('update-wishlist/', UpdateWishList.as_view()),
    path('process-order/', ProcessOrder.as_view()),
    path('order-data/', GetOrder.as_view()),
]

urlpatterns += [
    path('cart/', FetchCart.as_view()),
    path('add-to-cart/', AddItemToCart.as_view()),
    path('save-cart/', SaveCart.as_view())
]