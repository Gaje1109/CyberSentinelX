
from django.urls import path, include
#urlpatterns = [path('email/', include('emailScanner.urls'))
#]

from django.urls import path
from .views import classify_email_view

urlpatterns = [
    path('classify/', classify_email_view, name='classify_email'),
]