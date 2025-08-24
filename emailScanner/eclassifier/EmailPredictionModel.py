import joblib
import os

## Load saved model and vectorizer

EMAIL_MODEL_PATH = os.path.join(os.path.dirname(__file__), 'model')
email_model = joblib.load(os.path.join(EMAIL_MODEL_PATH, 'emailModel.pkl'))
email_vectorizer = joblib.load(os.path.join(EMAIL_MODEL_PATH, 'vectorizer.pkl'))

def predict_email(email):
    email_vector = email_vectorizer.transform([email])
    label = email_model.predict(email_vector)[0]
    confidence = email_model.predict_proba(email_vector).max()
    return (
        "SCAM" if label == 1 else "HAM",round(confidence*100,2)
    )
