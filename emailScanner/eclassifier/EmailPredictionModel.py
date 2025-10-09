import joblib
import os
import re
import requests


## Load saved model and vectorizer
EMAIL_MODEL_PATH = os.path.join(os.path.dirname(__file__), 'model')
email_model = joblib.load(os.path.join(EMAIL_MODEL_PATH, 'emailModel.pkl'))
email_vectorizer = joblib.load(os.path.join(EMAIL_MODEL_PATH, 'vectorizer.pkl'))

def predict_email_text(email):
    email_vector = email_vectorizer.transform([email])
    label = email_model.predict(email_vector)[0]
    confidence = email_model.predict_proba(email_vector).max()
    return ("SPAM", round(confidence * 100, 2)) if label == 1 else ("HAM", round(confidence * 100, 2))

## Extraction and Scanning logic
def extract_urls(text):
    """
    Extracts all URLs from a block of text.
    """
    # A robust regex for finding URLs
    url_pattern = r'(?:(?:https?|ftp):\/\/)?[\w/\-?=%.]+\.[\w/\-?=%.]+'
    return re.findall(url_pattern, text)


def verify_urls_with_level2(urls):
    """
    Checks a list of URLs against the Google Safe Browsing API.
    Returns a list of URLs that were flagged as unsafe.
    """
    if not urls:
        return []

    KEY = 'AIzaSyDb7Ii618KAtbydXwgdVNYKrzZXeyxRkzY'
    api_url = f'https://safebrowsing.googleapis.com/v4/threatMatches:find?key={KEY}'

    payload = {
        'client': {'clientId': 'email-scanner-app', 'clientVersion': '1.0'},
        'threatInfo': {
            'threatTypes': ['MALWARE', 'SOCIAL_ENGINEERING', 'UNWANTED_SOFTWARE', 'POTENTIALLY_HARMFUL_APPLICATION'],
            'platformTypes': ['ANY_PLATFORM'],
            'threatEntryTypes': ['URL'],
            'threatEntries': [{'url': url} for url in urls]
        }
    }

    flagged_urls = []
    try:
        with requests.Session() as session:
            response = session.post(api_url, json=payload, timeout=15)
            response.raise_for_status()
            data = response.json()
            if 'matches' in data:
                flagged_urls = [match['threat']['url'] for match in data['matches']]
    except requests.exceptions.RequestException as e:
        print(f"Error calling Google Safe Browsing API: {e}")

    return flagged_urls


# --- NEW: Master Analysis Function ---
def analyze_email_holistically(email_content):
    """
    Performs a multi-layered analysis of the email content.
    Returns a dictionary with a detailed verdict.
    """
    # Step 1: Get the prediction from your text-based model
    text_prediction, confidence = predict_email_text(email_content)

    # Step 2: Extract and scan all URLs in the email body
    urls = extract_urls(email_content)
    flagged_urls = verify_urls_with_level2(urls)

    # Step 3: Make the final decision based on all evidence
    if flagged_urls:
        return {
            "result": "SPAM",
            "confidence": 100.0,
            "reason": "Malicious link detected",
            "details": f"The following unsafe links were found: {', '.join(flagged_urls)}"
        }

    if text_prediction == "SPAM":
        return {
            "result": "SPAM",
            "confidence": confidence,
            "reason": "Suspicious text content",
            "details": "The email content matches patterns of known spam."
        }

    # If all checks pass
    return {
        "result": "HAM",
        "confidence": confidence,
        "reason": "Content and links appear safe",
        "details": "No immediate threats were detected."
    }