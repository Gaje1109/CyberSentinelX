import os, django
os.environ.setdefault("DJANGO_SETTINGS_MODULE","cyberSniffer.settings")
print(">>> Before django.setup()")
django.setup()
#os.environ.setdefault("DJANGO_SETTINGS_MODULE", "cyberSniffer.settings")

#django.setup()
print(">>> After django.setup()")
