import pandas as pd
from excelScanner.excelClassifier.extract_features import extract_features
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
import joblib

def train_new_model(input_feature_file):
    df = pd.read_csv(input_feature_file)
    print("Data loaded and read : ", df.head())
    # Encode class labels
    from sklearn.preprocessing import LabelEncoder
    le = LabelEncoder()
    df["class"] = le.fit_transform(df["class"])

    X = df.drop(columns=["class"])
    y = df["class"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    clf = RandomForestClassifier(n_estimators=200, max_depth=20, class_weight="balanced", random_state=42)
    clf.fit(X_train, y_train)

    print(classification_report(y_test, clf.predict(X_test)))
    joblib.dump(clf, "models/url_rf_model.pkl")
    joblib.dump(le, "models/label_encoder.pkl")


def process_urls_from_df(input_csv, output_csv):
    df = pd.read_csv(input_csv, delimiter=",").head(1000)
    feature_rows=[]
    print("Shape of CSV:", df.shape)
    print("Columns:", df.columns.tolist())
    print(df.head(5))
    ###########################
    count_success, count_fail = 0, 0

    for idx, row in df.iterrows():
        url = row["url"]
        label = row["type"]

        try:
            features = extract_features(url)
            feature_rows.append(features + [label])
            count_success += 1
        except Exception as e:
            print(f"❌ Row {idx} failed: {url} → {e}")
            count_fail += 1

        if idx % 1000 == 0:  # Print progress every 1000 rows
            print(f"Processed {idx} rows. Success: {count_success}, Fail: {count_fail}")

        print(f"✅ Completed: Success={count_success}, Fail={count_fail}")
        columns = [
    "UsingIP","LongURL","ShortURL","Symbol@","Redirecting//","PrefixSuffix-",
    "SubDomains","HTTPS","DomainRegLen","Favicon","NonStdPort","HTTPSDomainURL",
    "RequestURL","AnchorURL","LinksInScriptTags","ServerFormHandler","InfoEmail",
    "AbnormalURL","WebsiteForwarding","StatusBarCust","DisableRightClick",
    "UsingPopupWindow","IframeRedirection","AgeofDomain","DNSRecording",
    "WebsiteTraffic","PageRank","GoogleIndex","LinksPointingToPage","StatsReport",
    "class"
]

        print("Inside process_urls_from_df")
        feature_df = pd.DataFrame(feature_rows, columns=columns)
        feature_df.to_csv(output_csv, index=False)
        print(f"Features saved to {output_csv}")
        print(f"Extracted rows: {len(feature_df)}")
        #return output_csv

# -------------------------------
# ENTRY POINT
# -------------------------------
if __name__ == "__main__":
    feature_file = process_urls_from_df("D:/Gajendran/Python Virtual Environments/CyberSentinelX/malicious_phish.csv","D:/Gajendran/Python Virtual Environments/CyberSentinelX/malcious_url_features.csv" )
    #train_new_model(feature_file)
