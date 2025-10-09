from django.shortcuts import render

from .models import Link, Question
from .forms import LinkForm
from linkScanner import scanner
import requests
import os
import tempfile
from django.http import FileResponse
from django.core.mail import EmailMessage
from django.conf import settings
from excelScanner.excelClassifier.excel_url_processor import process_excel, checkemailsyntax


def home(request):
    link_list = Link.objects.all()
    context =""

    if request.method == 'POST':
        original_link = request.POST.get('link', '').strip()

        ## Link wihtout protocols
        if not original_link:
            return render(request, 'linkScanner/index.html', {'link_list': link_list})

        if not original_link.lower().startswith(('http://', 'https://')):
            processed_link = 'https://' + original_link
        else:
            processed_link = original_link

        existing_link = Link.objects.filter(link=processed_link).first()


        ### existing link prediction
        if existing_link:
            final_result = existing_link.status
            model_result = scanner.check(processed_link)
            print("Model Prediction in progress:", model_result)
            if model_result == 'Bad':
                context = verify_and_save_link(processed_link, model_result, original_link)
                return render(request, 'linkScanner/result.html', context)
            ## Prediction Bad, so examination
            if final_result == 'Bad':
                context = verify_and_save_link(processed_link, model_result, original_link)
                print("Model Prediction in progress:", model_result)
                return render(request, 'linkScanner/result.html', context)

            else:
                context = {
                    'result': existing_link.status,
                    'display_url': original_link,
                    'target_url': processed_link,
                    'output': {'message': 'Result was retrieved from cache.'}
                }

                return render(request, 'linkScanner/result.html', context)

        ## Non- existing linke- direct examination
        else:
            #  Model's opinion first.
            model_result = scanner.check(processed_link)
            print(f"Your model result: '{model_result}'")

            context = verify_and_save_link(processed_link, model_result, original_link)
            return render(request, 'linkScanner/result.html', {'result': context, 'target_url': processed_link})

    return render(request, 'linkScanner/index.html', {'link_list': link_list})

def verify_and_save_link(processed_link, model_result,original_link):
    print(f"Checking '{processed_link}' with Google Safe Browsing...")
    google_is_unsafe = False  # Default to safe unless Google says otherwise.
    output = None

    # SECURITY WARNING: Use environment variables for your API key in production!
    KEY = 'AIzaSyDb7Ii618KAtbydXwgdVNYKrzZXeyxRkzY'

    api_url = f'https://safebrowsing.googleapis.com/v4/threatMatches:find?key={KEY}'

    payload = {
        'client': {'clientId': 'your-django-app', 'clientVersion': '1.0'},
        'threatInfo': {
            'threatTypes': ['MALWARE', 'SOCIAL_ENGINEERING', 'UNWANTED_SOFTWARE',
                            'POTENTIALLY_HARMFUL_APPLICATION'],
            'platformTypes': ['ANY_PLATFORM'],
            'threatEntryTypes': ['URL'],
            'threatEntries': [{'url': processed_link}]
        }
    }

    try:
        response = requests.post(api_url, json=payload)
        response.raise_for_status()

        output = response.json()
        # If the response has 'matches', Google has flagged the URL.
        if output:
            google_is_unsafe = True
            print("Google Safe Browsing CONFIRMS URL is UNSAFE.")
        else:
            print("Google Safe Browsing reports URL is SAFE.")

    except requests.exceptions.RequestException as e:
        print(f"Error calling Google API: {e}. The check will be skipped.")

    # 3. Determine the final result based on a clear hierarchy.
    if google_is_unsafe:
        # If Google says it's unsafe, that is the final verdict. It's authoritative.
        final_result = 'Unsafe'
    elif model_result.lower() == 'unsafe':
        # If Google says it's safe, but your model is suspicious, you can create a "Caution" status.
        final_result = 'Caution'  # Or 'Potentially Unsafe'
    else:
        # If both Google and your model say it's safe, then it's safe.
        final_result = 'Safe'

    # --- END: NEW UNCONDITIONAL SCANNING LOGIC ---

    # Save the final, combined result to the database.
    new_link = Link(link=processed_link, status=final_result)
    new_link.save()

    context = {
        'result': final_result,
        'target_url': original_link,
        'output': output
    }

    return context

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