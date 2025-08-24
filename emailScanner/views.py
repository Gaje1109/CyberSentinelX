from django.shortcuts import render

import os

from emailScanner.eclassifier.EmailPredictionModel import predict_email
from emailScanner.eclassifier.ExtractTextFromPdf import extractText_From_PDF


def classify_email_view(request):
    result, confidence = None, None

    if request.method == "POST":
        email_content = request.POST.get('email_content', '').strip()

        if 'email_file' in request.FILES:
            file = request.FILES['email_file']
            file_path = os.path.join('temp', file.name)
            os.makedirs(os.path.dirname(file_path), exist_ok=True)
            with open(file_path, 'wb+') as destination:
                for chunck in file.chunks():
                    destination.write(chunck)

            email_content = extractText_From_PDF(file_path)

        if email_content.strip():
            result, confidence = predict_email(email_content)

    return render(request, 'emailScanner/classify_email.html',{
    'result':result,
    'confidence':confidence})