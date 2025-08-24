from django.urls import path
from emailScanner.views import classify_email_view

urlpatterns = [
    path('classify/', classify_email_view, name ="Classification of Emails")]