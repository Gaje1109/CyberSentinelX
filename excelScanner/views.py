# excelScanner/views.py
import os
import tempfile
from django.shortcuts import render
from django.http import FileResponse, JsonResponse
from excelScanner.excelClassifier.excel_url_processor import process_excel, checkemailsyntax, call_url_excel, callemail

def upload_excel_latest(request):
    if request.method == "POST":
        excel_file = request.FILES.get("excel_file")
        email = request.POST.get("email")
        output_name = f"Updated_{excel_file.name}"
        output_path = os.path.join(tempfile.gettempdir(), output_name)

        # Excel Null check
        if not excel_file:
            return render(request, "linkScanner/excel_upload.html", {"error": "No file uploaded"})
        # Email null check
        if not email and not str(email).strip():
            return render(request, "linkScanner/excel_upload.html", {"error":"Email is  null/blank"})
        # Email syntax check
        emailauthenticity = checkemailsyntax(email)

        print("Email Id is : ",emailauthenticity)

        #if emailauthenticity !="true":
           # return render(request, "linkScanner/excel_upload.html", {"error":"Invalid Email Addresss"})

        l1_output_file = process_excel(excel_file, output_path)
        print(l1_output_file)
        if l1_output_file is not None:
            print("Passed L1")

            #return render(request, "linkScanner/excel_upload.html", {"success": "File uploaded successfully!"})
        print("Level 1 excel processed")
        l2_output_file= call_url_excel(l1_output_file)
        if l2_output_file is not None:
            print("Passed L2")
            print(l2_output_file)
           # return render(request, "linkScanner/excel_upload.html", {"success": "Validation completed successfully!"})
        print("Level 2 excel processed")
        callemail(l2_output_file, email)

        return render(request, "linkScanner/excel_upload.html", {"success": "Email sent successfully!"})

    # 👇 Corrected template path
    return render(request, "linkScanner/excel_upload.html")