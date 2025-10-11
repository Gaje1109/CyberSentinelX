import numpy as np
import pandas as pd
import pickle
import re
import os
import requests
import time
from django.conf import settings
from dotenv import load_dotenv

###########################################
from excelScanner.excelClassifier.readssl import call_read_ssl,sendemail
from linkScanner.scanner import FeatureExtraction
###########################################

# Load trained model
### Commented for Docker
#BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
#model =  pickle.load(open('D:\Gajendran\Python Virtual Environments\CyberSentinelX\phishing_forestclassifier.pkl', 'rb'))


MODEL_PATH = os.path.join(settings.BASE_DIR, "artifacts", "phishing_forestclassifier.pkl")
with open(MODEL_PATH, "rb") as f:
    model = pickle.load(f)

load_dotenv()
# Human-readable labels for each feature index
FEATURE_NAMES = [
    "Using IP instead of domain",
    "Long URL",
    "Shortened URL",
    "Contains '@' symbol",
    "Multiple '//' redirections",
    "Prefix/Suffix '-' in domain",
    "Suspicious Subdomains",
    "HTTPS check",
    "Short Domain Registration length",
    "Favicon mismatch",
    "Non-standard port",
    "HTTPS in domain name",
    "Suspicious Request URL",
    "Suspicious Anchor URL",
    "Links in Script Tags",
    "Server Form Handler issue",
    "Email in URL/Content",
    "Abnormal URL",
    "Website Forwarding",
    "Status Bar Customization",
    "Disable Right Click",
    "Popup Window usage",
    "Iframe Redirection",
    "Young Domain (Age < 6 months)",
    "DNS Recording issue",
    "Low Website Traffic",
    "Low PageRank",
    "Not indexed by Google",
    "Suspicious External Links",
    "Reported in Stats/Blacklist"
]

## Call the ReadWriteURLSSL method
def call_url_excel(l1_updated_file):
   l2_output_file=  call_read_ssl(l1_updated_file)
   return l2_output_file

## Check it against the features
def explain(features):
    """
    Given the 30 features (list of ints), return reasons for risky (-1) values.
    """
    reasons = []
    for idx, val in enumerate(features):
        if val == -1:
            reasons.append(FEATURE_NAMES[idx])
    if not reasons:
        return "No obvious risk"
    return "; ".join(reasons)

## Main Processing the incoming excel Files
def process_excel(input_file, output_file):
    df = pd.read_excel(input_file, engine="openpyxl")
    url_col = "REQUEST URL" if "REQUEST URL" in df.columns else "request url"
    results = []

    with requests.Session() as session:
        for rawurl in df[url_col]:
            final_status = "Error"
            reason = "An unexpected error occurred."
            tertiary_ratio = "Not Checked"
            try:
                url = str(rawurl).strip()
                if not url.lower().startswith(('http://', 'https://')):
                    url = 'https://' + url

                # Level 1: Local Model Prediction
                model_label = "Safe" if model.predict(np.array(FeatureExtraction(url).getFeaturesList()).reshape(1, 30))[0] == 1 else "Unsafe"

                # Level 2: Tuned model Prediction
                secondary_label = verify_the_excel_urls_v2(session, url)

                # --- Authoritative Decision Logic with All Edge Cases --
                if secondary_label == 'Unsafe':
                    # Rule 1: Primary service is authoritative.
                    final_status = 'Unsafe'
                    reason = 'Flagged as malicious by a secondary threat intelligence service.'

                elif model_label == 'Unsafe' and secondary_label == 'Safe':
                    # Rule 2: Conflict triggers a deep scan.
                    tertiary_status, tertiary_ratio = verify_the_excel_urls_v3(session, url)
                    if tertiary_status == 'Unsafe':
                        final_status = 'Unsafe'
                        reason = f'Confirmed malicious by deep analysis. (Detection Ratio: {tertiary_ratio})'
                    else:
                        final_status = 'Unsafe'  # Trust the initial heuristic model's suspicion
                        reason = f'Flagged by local heuristics; deep analysis found no widespread threat. (Ratio: {tertiary_ratio})'

                elif model_label == 'Unsafe' and secondary_label == 'Unsafe':
                    # Rule 3: High confidence triggers a confirmation deep scan.
                    final_status = 'Unsafe'
                    tertiary_status, tertiary_ratio = verify_the_excel_urls_v3(session, url)
                    reason = f'Confirmed malicious by multiple services. (Deep Analysis Ratio: {tertiary_ratio})'

                elif secondary_label == 'API Error':
                    # Rule 4 (Edge Case): Fallback to local model if primary service fails.
                    final_status = model_label
                    reason = f'Classified as {model_label} by local heuristics (Threat intelligence service was unavailable).'

                else:  # Rule 5 (Default): Both checks agree the URL is safe.
                    final_status = 'Safe'
                    reason = 'Considered safe by all primary checks.'
            except Exception as e:
                results.append(
                    {"REQUEST URL": rawurl, "Final Status": "Processing Error", "VirusTotal Detections": "N/A",
                     "Reason": str(e)})

            results.append({
                "REQUEST URL": rawurl,
                "MODEL PREDICTION RESULT": final_status,
                "ANALYSIS DETECTION RESULT": tertiary_ratio,
                "COMMENT": reason
            })


    result_df = pd.DataFrame(results)
    result_df.to_excel(output_file, index=False, engine="openpyxl")
    print(f"Results with verification saved to {output_file}")
    return output_file

# 2nd Level Validation
def verify_the_excel_urls_v2(session, url):
    SECOND_KEY = os.getenv('SECOND_KEY')
    SECOND_URL = os.getenv('SECOND_VALIDATION_API_URL')
    api_url = f'{SECOND_URL}{SECOND_KEY}'

    payload = {
        'client': {'clientId': 'excel-scanner-app', 'clientVersion': '1.0'},
        'threatInfo': {
            'threatTypes': ['MALWARE', 'SOCIAL_ENGINEERING', 'UNWANTED_SOFTWARE', 'POTENTIALLY_HARMFUL_APPLICATION'],
            'platformTypes': ['ANY_PLATFORM'], 'threatEntryTypes': ['URL'], 'threatEntries': [{'url': url}]}
    }

    try:
        response = session.post(api_url, json = payload, timeout= 13)
        response.raise_for_status()

            # If the response has 'matches'
        if response.json():
            print(f"Confirmed '{url}' is UNSAFE")
            return 'Unsafe'

        else:
            print(f"Confirmed '{url}' is SAFE")
            return 'Safe'
    except requests.exceptions.RequestException as e:
        print(f"Error calling the URL '{url}': {e}"f"")
        return 'URL hit error'

## 3rd level validation
def verify_the_excel_urls_v3(session , url):
    THREE_KEY =os.getenv('THREE_KEY')
    THIRD_URL = os.getenv('THIRD_VALIDATION_API_URL')

    headers = {'x-apikey': THREE_KEY}
    try:
        # Submit URL and get the analysis report ID
        response = session.post(THIRD_URL, headers=headers, data={'url': url},
                                timeout=10)
        response.raise_for_status()
        analysis_id = response.json()['data']['id']

        # Wait for the report to be generated
        time.sleep(15)

        report_url = f'https://www.virustotal.com/api/v3/analyses/{analysis_id}'
        response = session.get(report_url, headers=headers, timeout=10)
        response.raise_for_status()

        stats = response.json()['data']['attributes']['stats']
        malicious_vendors = stats.get('malicious', 0)
        total_vendors = sum(stats.values())

        status = 'Unsafe' if malicious_vendors > 0 else 'Safe'
        ratio = f"{malicious_vendors}/{total_vendors}"
        return status, ratio

    except requests.exceptions.RequestException as e:
        print(f"ERROR: Validation 3 call failed for {url}: {e}")
        return 'API Error', 'N/A'
    except KeyError:
        return 'API Error', 'Parsing Failed'

## verify email syntax
def checkemailsyntax(email: str) -> bool:
    if not email or not str(email).strip():
        return False

    # Simple RFC 5322 compliant regex
    pattern = r'^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    return re.match(pattern, email) is not None
def callemail(output_path, email):

    print("output_path " ,output_path)
    sendemail(output_path, email)

'''
if __name__ == "__main__":
    input_file = r"D:\Gajendran\Python Virtual Environments\CyberSentinelX\MAH_20240121_153659_1.xlsx"
    output_file = r"D:\Gajendran\Python Virtual Environments\CyberSentinelX\MAH_20240121_153659_1.xlsx"
    #input_file = r"/MAH_20240121_153659_1.xlsx"
    #output_file = r"/MAH_20240121_153659_1/xlsx"


    process_excel(input_file, output_file)
    print("Inside excel")
    call_url_excel(input_file)
'''