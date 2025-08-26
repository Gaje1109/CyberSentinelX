import os
import sys

# Ensure the project directory is on the Python path
# This is crucial if your project structure is non-standard
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if BASE_DIR not in sys.path:
    sys.path.append(BASE_DIR)

from django.core.wsgi import get_wsgi_application

# Add a print statement to verify the settings module
settings_module = 'cyberSniffer.settings'
print(f"Loading settings from: {settings_module}")

os.environ.setdefault('DJANGO_SETTINGS_MODULE', settings_module)

application = get_wsgi_application()
