from django.urls import path
from excelScanner.views import upload_excel_latest
urlpatterns = [
    path('upload-excel/', upload_excel_latest, name="Upload to Excel")]
