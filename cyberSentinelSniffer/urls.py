"""
URL configuration for CyberSentinelX project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from linkScanner import views
#from emailScanner import views
#from excelScanner import views
from django.conf import settings
from django.conf.urls.static import static
## EmailScanner Integration
from django.urls import path, include




urlpatterns = [
    path('email/', include('emailScanner.urls')),
    path('excel/', include('excelScanner.urls')),
    path('admin/', admin.site.urls),
    path('',views.home),
    path('learn/', views.learn),
    path('report/', views.report),
    path('contact/', views.contact),
    path('about/', views.about),
    path('result/', views.result),
    path('module1/',views.module1),
    path('module2/',views.module2),
    path('module3/',views.module3),
    path('game1/',views.game1),
    path('game2/',views.game2),
    path('game3/',views.game3)
]
