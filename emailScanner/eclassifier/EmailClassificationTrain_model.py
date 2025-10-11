import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
import joblib
import os

## TRAINING THE EMAIL CLASSIFICATION FOR SPAM AND HAM
# Load the Dataset
email_df = pd.read_csv('/emailScanner/dataset/spam.csv', encoding='latin-1')
email_df = email_df[['v1','v2']]
print("1st check point: ",email_df)
email_df.columns =['label','text']
email_df['label'] = email_df['label'].map({
    'ham':0,
    'spam':1
})

## Split the Data
X_train, X_test, Y_train, Y_test = train_test_split(email_df['text'], email_df['label'], test_size=0.2, random_state=42)

## TF-IDF Vectorizer
vectorizer = TfidfVectorizer()
X_train_vectors = vectorizer.fit_transform(X_train)
X_test_vectors = vectorizer.transform(X_test)

## Train model
emailModel = LogisticRegression()
emailModel.fit(X_train_vectors, Y_train)

## Evaluate
predicted_email_model = emailModel.predict(X_test_vectors)
print("Accuracy: ",accuracy_score(Y_test, predicted_email_model))

## Save the model and vectorizer

model_directory = "emailScanner/eclassifier/model"
os.makedirs(model_directory, exist_ok=True)
#os.mkdir("D:/Gajendran/Python Virtual Environments/CyberSentinelX/emailScanner/eclassifier/model")
#os.mkdir("D:/Gajendran/Python Virtual Environments/CyberSentinelX/emailScanner/eclassifier/model")
#joblib.dump(emailModel, "D:/Gajendran/Python Virtual Environments/CyberSentinelX/emailScanner/eclassifier/model/emailModel.pkl")
#joblib.dump(vectorizer, "D:/Gajendran/Python Virtual Environments/CyberSentinelX/emailScanner/eclassifier/model/vectorizer.pkl")
joblib.dump(vectorizer, os.path.join(model_directory, "vectorizer.pkl"))
joblib.dump(emailModel, os.path.join(model_directory, "emailModel.pkl"))

print("Model Saved")