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
from django.urls import path, include, re_path
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    # apps (accepts /email and /email/)
    path('email/', include('emailScanner.urls')),
    path('excel/', include('excelScanner.urls')),
    path('admin/', admin.site.urls),
    path('', views.home),
    re_path(r"^learn/?$", views.learn, name="learn"),
    re_path(r"^report/?$", views.report, name="report"),
    re_path(r"^contact/?$", views.contact, name="contact"),
    re_path(r"^about/?$", views.about, name="about"),
    re_path(r"^result/?$", views.result, name="result"),
    re_path(r"^module1/?$", views.module1, name="module1"),
    re_path(r"^module2/?$", views.module2, name="module2"),
    re_path(r"^module3/?$", views.module3, name="module3"),
    re_path(r"^game1/?$", views.game1, name="game1"),
    re_path(r"^game2/?$", views.game2, name="game2"),
    re_path(r"^game3/?$", views.game3, name="game3"),
]
# urlpatterns = [
#     path('email/', include('emailScanner.urls')),
#     path('excel/', include('excelScanner.urls')),
#      path('admin/', admin.site.urls),
#      path('',views.home),
#     path('learn/', views.learn),
#     path('report/', views.report),
#     path('contact/', views.contact),
#     path('about/', views.about),
#     path('result/', views.result),
#     path('module1/',views.module1),
#     path('module2/',views.module2),
#     path('module3/',views.module3),
#     path('game1/',views.game1),
#     path('game2/',views.game2),
#     path('game3/',views.game3)
# ]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
