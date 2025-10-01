# excelScanner/views.py
import os
import tempfile
from pathlib import Path
from django.shortcuts import render
from django.core.exceptions import ValidationError
from django.views.decorators.http import require_http_methods
from django.conf import settings
from excelScanner.excelClassifier.excel_url_processor import process_excel, checkemailsyntax, call_url_excel, callemail

ALLOWED_EXT = {".xlsx", ".xls"}

def validate_excel_file(f):
    ext = Path(f.name).suffix.lower()

    if ext not in ALLOWED_EXT:
        raise ValidationError("Inside excelScanner/views.py : Please upload an Excel file (.xlsx or .xls) and try again.")

@require_http_methods(["GET", "POST"])
def upload_excel_latest(request):

    if request.method == "GET":
        return render(request, "linkScanner/excel_upload.html", {"error": "No file uploaded"})

    ## POST
    #if request.method == "POST":

    excel_file = request.FILES.get("excel_file")
    email = (request.POST.get("email") or "").strip()
    #############################################################################################################################
    # Excel Null check
    if not excel_file:
        return render(request, "linkScanner/excel_upload.html", {"error": "No file uploaded"})
    #############################################################################################################################
    # Email null check
    if not email: # and not str(email).strip():
        return render(request, "linkScanner/excel_upload.html", {"error": "Email is  null/blank"})
    #############################################################################################################################
    # Email syntax check
    try:
        if not checkemailsyntax(email):
            return render(request, "linkScanner/excel_upload.html", {"error": "Invalid email address"})

    except Exception as e:
        print("Email Validation Failed")
        return  render(request, "linkScanner/excel_upload.html", {"error": "Email validation error."})


    #############################################################################################################################
    try:
        validate_excel_file(excel_file)

    except ValidationError as ve:
        return render(request, "linkScanner/excel_upload.html", {"error": str(ve)})
    #############################################################################################################################
    ## Create unique temp output file
    try:
        with tempfile.NamedTemporaryFile(prefix= "Updated_", suffix = Path(excel_file.name).suffix, delete=False) as tmp:
            output_path = tmp.name
    except Exception as e:
        print("Failed to Create temp file")
        return render(request, "linkScanner/excel_upload.html", {"error": "Internal temp file error."})
    #############################################################################################################################
    try:
        # L1: local Excel processing; pass the uploaded file object and desired output path
        l1_output_file = process_excel(excel_file, output_path)
        if not l1_output_file or not os.path.exists(l1_output_file):
            return render(request, "linkScanner/excel_upload.html", {"error": "Processing Failed - L1"})

        print("L1 OK : %s", l1_output_file)
        #############################################################################################################################
        # L2: Java Processor
        l2_output_file = call_url_excel(l1_output_file)
        if not l2_output_file is not os.path.exists(l2_output_file):
            return render(request, "linkScanner/excel_upload.html", {"error": "Processing Failed - L2"})

        print("L2 OK : %s", l2_output_file)
        #############################################################################################################################
        ## Result to be mailed
        callemail(l2_output_file, email)

        print("Email sent successfully")
        #############################################################################################################################
    except Exception as e:
        print("Pipeline Error at excelScanner/views.py")
        return render(request, "linkScanner/excel_upload.html", {"success": f"Processing Failed!! {e}"})

    # 👇 Corrected template path
    return render(request, "linkScanner/excel_upload.html", {"success": "Email sent successfully!"})