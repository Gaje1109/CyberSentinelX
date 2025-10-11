import os
import uuid
import tempfile
import shutil
from pathlib import Path
from django.shortcuts import render
from django.http import JsonResponse
from django.core.exceptions import ValidationError
from django.views.decorators.http import require_http_methods
from django.conf import settings
from excelScanner.excelClassifier.excel_url_processor import process_excel, checkemailsyntax, call_url_excel, callemail

ALLOWED_EXT = {".xlsx", ".xls"}


def validate_excel_file(f):
    ext = Path(f.name).suffix.lower()
    if ext not in ALLOWED_EXT:
        raise ValidationError("Please upload an Excel file (.xlsx or .xls).")


@require_http_methods(["GET", "POST"])
def upload_excel_latest(request):
    if request.method == "GET":
        return render(request, "linkScanner/excel_upload.html")

    excel_file = request.FILES.get("excel_file")
    email = (request.POST.get("email") or "").strip()

    # --- Validation ---
    if not excel_file:
        return JsonResponse({"error": "No file was uploaded."}, status=400)
    if not email:
        return JsonResponse({"error": "Email address is required."}, status=400)
    if not checkemailsyntax(email):
        return JsonResponse({"error": "Invalid email address format."}, status=400)

    try:
        validate_excel_file(excel_file)
    except ValidationError as ve:
        return JsonResponse({"error": str(ve)}, status=400)

    # --- File Processing ---
    l1_temp_file_path = None
    l2_temp_file_path = None
    try:
        original_filename = Path(excel_file.name)
        temp_dir = os.path.join(settings.MEDIA_ROOT, 'excel_temp')
        try:
            os.makedirs(temp_dir, exist_ok=True)
            # Test write permissions
            test_file_path = os.path.join(temp_dir, f"test_{uuid.uuid4().hex}")
            with open(test_file_path, 'w') as f:
                f.write('test')
            os.remove(test_file_path)
        except PermissionError:
            print("CRITICAL DOCKER ERROR: Permission denied to write to the temp directory.")
            error_msg = "Internal Server Error: The application does not have permission to write temporary files. Please check Docker volume permissions."
            return JsonResponse({"error": error_msg}, status=500)

        # --- Continue with processing ---
        l1_temp_filename = f"L1_output_{uuid.uuid4().hex[:12]}{original_filename.suffix}"
        l1_temp_file_path = os.path.join(temp_dir, l1_temp_filename)

        process_excel(excel_file, l1_temp_file_path)
        if not os.path.exists(l1_temp_file_path):
            return JsonResponse({"error": "Processing Failed - L1 (Python model did not create an output file)."},
                                status=500)
        print(f"L1 OK: {l1_temp_file_path}")


        l2_temp_file_path = call_url_excel(l1_temp_file_path)

        if not isinstance(l2_temp_file_path, str) or not l2_temp_file_path.strip() or not os.path.exists(
                l2_temp_file_path):
            return JsonResponse(
                {"error": "Processing Failed - L2 (Java processor returned an invalid or non-existent file path)."},
                status=500)
        print(f"L2 OK: {l2_temp_file_path}")

        source_file_for_move = l2_temp_file_path # l2_temp_file_path
        unique_id = uuid.uuid4().hex[:8]
        final_filename = f"{original_filename.stem}_processed_{unique_id}{original_filename.suffix}"
        final_public_path = os.path.join(settings.MEDIA_ROOT, final_filename)

        shutil.move(l1_temp_file_path, final_public_path)

        # This ensures that even if the email fails, the user still gets a download link.
        try:
            callemail(final_public_path, email)
            print("Email sent successfully")
        except Exception as email_error:
            print(f"CRITICAL: Failed to send email. Error: {email_error}")
            # We don't stop the process, the user can still download the file.

        download_url = os.path.join(settings.MEDIA_URL, final_filename)

        return JsonResponse({"success": True, "download_url": download_url})

    except Exception as e:
        print(f"Pipeline Error at excelScanner/views.py: {e}")
        return JsonResponse({"error": f"An unexpected server error occurred during processing."}, status=500)

    finally:
        # Cleanup any intermediate files that might be left over
        if l1_temp_file_path and os.path.exists(l1_temp_file_path):
            os.remove(l1_temp_file_path)
        if l2_temp_file_path and isinstance(l2_temp_file_path, str) and os.path.exists(l2_temp_file_path):
            os.remove(l2_temp_file_path)

