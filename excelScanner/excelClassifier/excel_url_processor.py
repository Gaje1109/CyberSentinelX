import numpy as np
import pandas as pd
import pickle
import re
import os
import requests

###########################################
from excelScanner.excelClassifier.readssl import call_read_ssl,sendemail
from linkScanner.scanner import FeatureExtraction
###########################################

# Load trained model
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

MODEL_PATH = os.path.join(BASE_DIR, "artifacts", "phishing_forestclassifier.pkl")
with open(MODEL_PATH, "rb") as f:
    model = pickle.load(f)

#model =  pickle.load(open('D:\Gajendran\Python Virtual Environments\CyberSentinelX\phishing_forestclassifier.pkl', 'rb'))

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

## Process the incoming excel Files
def process_excel(input_file, output_file):
    df = pd.read_excel(input_file, engine="openpyxl")
    url_col = "REQUEST URL" if "REQUEST URL" in df.columns else "request url"
    results = []

    with requests.Session() as session:
        for rawurl in df[url_col]:
            try:
                url = str(rawurl).strip()
                if not url.lower().startswith(('http://', 'https://')):
                    url = 'https://' + url

                obj = FeatureExtraction(url)
                features = obj.getFeaturesList()
                prediction = model.predict(np.array(features).reshape(1, 30))[0]
                model_label = "Safe" if prediction == 1 else "Unsafe"

                google_label = verify_the_excel_urls_v2(session, url)

                # --- NEW: Authoritative Decision Logic ---
                final_status = ""
                reason = ""

                if google_label == 'Unsafe':
                    final_status = 'Unsafe'
                    reason = 'Flagged as Unsafe by Google Safe Browsing.'

                elif google_label == 'API Error':
                    # Edge Case: If the API fails, trust the local model's verdict
                    final_status = model_label
                    reason = f'Flagged as {model_label} by Local Model (Google API check failed).'

                else:  # Google API reports the URL is 'Safe'
                    final_status = model_label  # Trust the local model
                    if model_label == 'Unsafe':
                        reason = 'Flagged as Unsafe by Local Model (Google API reported Safe).'
                    else:
                        reason = 'Considered Safe by both Local Model and Google API.'

                results.append({
                    "REQUEST URL": rawurl,
                    "Final Status": final_status,
                    "Reason": reason
                })

            except Exception as e:
                results.append({
                    "REQUEST URL": rawurl,
                    "Final Status": "Error",
                    "Reason": str(e)
                })

    result_df = pd.DataFrame(results)
    result_df.to_excel(output_file, index=False, engine="openpyxl")
    print(f"Results with verification saved to {output_file}")
    return output_file

# Through verification
def verify_the_excel_urls_v2(session, url):
    KEY = 'AIzaSyDb7Ii618KAtbydXwgdVNYKrzZXeyxRkzY'

    api_url = f'https://safebrowsing.googleapis.com/v4/threatMatches:find?key={KEY}'

    payload = {
        'client': {'clientId': 'excel-scanner-app', 'clientVersion': '1.0'},
        'threatInfo': {
            'threatTypes': ['MALWARE', 'SOCIAL_ENGINEERING', 'UNWANTED_SOFTWARE', 'POTENTIALLY_HARMFUL_APPLICATION'],
            'platformTypes': ['ANY_PLATFORM'],
            'threatEntryTypes': ['URL'],
            'threatEntries': [{'url': url}]
        }
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