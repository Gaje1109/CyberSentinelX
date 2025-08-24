from django.shortcuts import render

from .models import Link, Question
from .forms import LinkForm
from linkScanner import scanner
import os
import tempfile
from django.http import FileResponse
from django.core.mail import EmailMessage
from django.conf import settings
from excelScanner.excelClassifier.excel_url_processor import process_excel, checkemailsyntax


# Create your views here.
def home(request):
    link_list = Link.objects.all()
    if request.method == 'POST':
        link = request.POST.get('link','')   
        existing_link = Link.objects.filter(link=link).first()
        if existing_link:
            result = existing_link.status
            target_url = request.POST.get('link')  
            return render(request,'linkScanner/result.html',{'result':result, 'target_url': target_url})
        else:
            result = scanner.check(str(link))
            status = request.POST.get('status',result)
            link = Link(link=link, status=status) #creating link object so that it gets saved in the database
            link.save()
            target_url = request.POST.get('link')   
            return render(request,'linkScanner/result.html',{'result':result, 'target_url': target_url})
    return render(request,'linkScanner/index.html',{'link_list':link_list})

def learn(request):
    return render(request, 'linkScanner/learn.html')


def report(request):
    if request.method == 'POST':
        reportLink = request.POST.get('reportLink', '')
        level = request.POST.get('level','')
        existing_link = Link.objects.filter(link=reportLink).first()
        if existing_link: # If the link already exists
            # existing_link.status = status
            # existing_link.save()
            print("exists")
            print(existing_link.status)
            if existing_link.status == "Good": 
                #retraining
                scanner.train_model(existing_link.link)                                     
                return render(request, 'linkScanner/result.html',{'result': "Thank you!"})
            else:  #if existing link is bad
                return render(request, 'linkScanner/result.html',{'result': "Thank you!"})     
      
        else:
            print("doesnt exist")
            result = scanner.check(str(reportLink))
            status = request.POST.get('status',result)
            link_list = Link.objects.all()
            link = Link(link=reportLink, status=status)
            link.save()
            if result == "Good":
                #retrain
                scanner.train_model(link.link)
                return render(request, 'linkScanner/result.html',{'result': "Thank you!"})
            else: 
                return render(request, 'linkScanner/result.html',{'result': "Thank you!"})
    return render(request, 'linkScanner/report.html')

def contact(request):
    return render(request, 'linkScanner/contact.html')

def about(request):
    return render(request, 'linkScanner/about.html')

def result(request):
    return render(request,'linkScanner/result.html')

def module1(request):
    return render(request,'linkScanner/module1.html')

def module2(request):
    return render(request,'linkScanner/module2.html')

def module3(request):
    return render(request,'linkScanner/module3.html')

def game1(request):
    return render(request,'linkScanner/game1.html')

def game2(request):
    return render(request,'linkScanner/game2.html')

def game3(request):
    return render(request,'linkScanner/game3.html')