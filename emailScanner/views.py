from django.shortcuts import render

import os
from emailScanner.eclassifier.EmailPredictionModel import analyze_email_holistically
from emailScanner.eclassifier.EmailPredictionModel import predict_email_text
from emailScanner.eclassifier.ExtractTextFromPdf import extractText_From_PDF


def classify_email_view(request):
    result, confidence = None, None
    file_path = None
    analysis_result = ""

    if request.method == "POST":
        try:

            email_content = request.POST.get('email_content', '').strip()

            if 'email_file' in request.FILES:
                file = request.FILES['email_file']

                temp_dir = os.path.join('temp_files')
                os.makedirs(temp_dir, exist_ok=True)

                # Create a unique name to avoid cnflicts
                file_path = os.path.join(temp_dir, f"upload_{file.name}")

                with open(file_path, 'wb+') as destination:
                    for chunk in file.chunks():
                        destination.write(chunk)

                email_content = extractText_From_PDF(file_path)
            # This one function now handles text analysis and URL scanning.
            if email_content.strip():
                analysis_result = analyze_email_holistically(email_content)

        finally:
            if file_path and os.path.exists(file_path):
                os.remove(file_path)
    return render(request, 'emailScanner/classify_email.html', {
        'analysis_result': analysis_result
    })