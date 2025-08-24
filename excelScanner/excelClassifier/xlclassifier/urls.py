from django.urls import path
from excelScanner.views import upload_excel
urlpatterns = [
    path('upload-excel/', upload_excel, name="Upload to Excel")]
