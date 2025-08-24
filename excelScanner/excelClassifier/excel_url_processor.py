import numpy as np
import pandas as pd
import pickle
import re
###########################################
from excelScanner.excelClassifier.readssl import call_read_ssl,sendemail
from linkScanner.scanner import FeatureExtraction
###########################################

# Load trained model
model =  pickle.load(open('D:\Gajendran\Python Virtual Environments\CyberSentinelX\phishing_forestclassifier.pkl', 'rb'))

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

    # Detect correct URL column
    if "REQUEST URL" in df.columns:
        url_col = "REQUEST URL"
    elif "request url" in df.columns:
        url_col = "request url"
    else:
        raise ValueError("No 'REQUEST URL' or 'url' column found in Excel file")

    results = []

    for rawurl in df[url_col]:
        try:
            url = str(rawurl).strip()
            # Extract features
            obj = FeatureExtraction(url)
            features = obj.getFeaturesList()

            # Predict
            prediction = model.predict(np.array(features).reshape(1, 30))[0]

            # Map prediction + reasons
            label = "Safe" if prediction == 1 else "Unsafe"
            reason = explain(features)

            results.append({
                "REQUEST URL": url,
                "Prediction": label,
               # "Reason": reason
            })

        except Exception as e:
            results.append({
                "REQUEST URL": rawurl,
                "Prediction": "Error",
               # "Reason": str(e)
            })

    # Save output Excel
    result_df = pd.DataFrame(results)
    result_df.to_excel(output_file, index=False, engine="openpyxl")
    print(f"Results with reasons saved to {output_file}")
    return output_file
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