# 🛡️ CyberSentinelX: AI-Driven Platform for Intelligent Phishing Detection and Threat Prevention

**CyberSentinelX** is a next-generation cybersecurity solution that unifies detection, education, and automation to create a resilient digital ecosystem. Leveraging **Machine Learning (ML)** and **Natural Language Processing (NLP)**, it provides real-time analysis to identify and mitigate evolving phishing and scam threats across URLs and email content.

|                                                                         | | |
|:-----------------------------------------------------------------------:| :---: | :---: |
|      ![Python](https://img.shields.io/badge/Python-3.11+-blue.svg)      | ![Django](https://img.shields.io/badge/Django-Framework-success.svg) | ![AWS](https://img.shields.io/badge/AWS-Cloud-orange.svg) |
| ![Terraform](https://img.shields.io/badge/Terraform-IaC-blueviolet.svg) | ![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-red.svg) | ![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED.svg) |
|     ![License](https://img.shields.io/badge/License-MIT-green.svg)      | | |

---

## 📘 Project Overview

This platform addresses the critical need for sophisticated, automated threat analysis. Deployed as a scalable **Django web application**, CyberSentinelX is built upon a robust **DevOps pipeline** utilizing **AWS, Terraform, Jenkins, and Docker**.

The core functionality is delivered through two specialized AI modules:

1.  **URLScanner**: Detects malicious links based on **lexical and domain-based features**.
2.  **EmailScanner**: Classifies the content of emails (text or PDF) using **NLP preprocessing** and **TF-IDF vectorization**.

### Architecture & Automation Flow

**`GitHub` → `Jenkins (CI/CD)` → `Docker (Containerization)` → `Terraform (IaC)` → `AWS (Cloud)`**

---

## 🧠 Key Features and Modules

### 🔍 Core Detection Modules

| Module | Purpose | Key Technologies |
| :--- | :--- | :--- |
| **URLScanner** | Identifies phishing URLs with a high-accuracy **Random Forest classifier**. | Lexical & Domain Feature Engineering, Random Forest |
| **EmailScanner** | Classifies email content (Scam/Ham) using advanced NLP techniques. | TF-IDF Vectorization, NLP Preprocessing, Classifier Models |
| **ExcelScanner** | **Bulk processing** of URLs via uploaded Excel files, generating downloadable and emailed reports. | AWS S3, AWS Lambda, AWS SES, Pandas |

### 🌐 Scalability and DevOps Integration

* **Infrastructure as Code (IaC):** **Terraform** provisions and manages the entire AWS cloud infrastructure ($\text{EC2, S3, Lambda, SES, SSM}$).
* **Continuous Integration/Continuous Deployment (CI/CD):** **Jenkins** automates the entire software lifecycle, from code build and testing to **Dockerization** and deployment.
* **Containerization:** **Docker** ensures a consistent, portable runtime environment across development, testing, and production.
* **Cloud Stack:** Hosted on **AWS Free Tier resources**, prioritizing cost-effective scalability and modularity.

### 🔄 User-Centric and Educational Components

* **Feedback & Retraining Loop:** Users can report false positives/negatives, providing critical, real-world data for scheduled **ML model retraining** to enhance long-term accuracy.
* **Learn Module:** Promotes **cybersecurity awareness** through accessible educational resources on current phishing patterns and proactive prevention strategies.

---

## ⚙️ Technical Objectives Achieved

* **Dual-Module Development:** Successfully developed and integrated **URLScanner** and **EmailScanner**.
* **Model Evaluation:** Trained and evaluated multiple ML algorithms ($\text{Random Forest, Naive Bayes, Logistic Regression}$).
* **Web Application Deployment:** Deployed a scalable, performant web application using the **Django framework**.
* **Full CI/CD Automation:** Established an end-to-end automation pipeline using **Docker, Jenkins, and Terraform**.
* **Portability and Modularity:** Designed the system for high modularity and ensured deployment consistency via containerization.

---

## 🧪 Testing and Validation

A rigorous testing strategy confirmed the system's robustness and accuracy:

| Test Type | Objective | Result Summary |
| :--- | :--- | :--- |
| **Unit Testing** | Check ML classification accuracy. | **$\checkmark$ Passed** ($\text{Accuracy >90%}$) |
| **Integration Testing** | Ensure seamless data flow across Django and AWS components. | **$\checkmark$ Passed** (Seamless sync) |
| **Performance Testing** | Evaluate system latency (real-time and bulk). | **$\checkmark$ Passed** ($\text{1.3s}$ per URL; $\text{<60s}$ for $\text{500}$ rows) |
| **Security Testing** | Validate $\text{HTTPS, SSL}$, and data hygiene (temporary file deletion). | **$\checkmark$ Passed** (Secure protocol standards met) |

---

## 🧰 Installation & Setup

### Prerequisites

* Python 3.10+
* $\text{git}$

### Local Environment Setup

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Gaje1109/CyberSentinelX.git
    cd CyberSentinelX
    ```
2.  **Create and Activate Virtual Environment:**
    ```bash
    python -m venv venv
    source venv/bin/activate    # Linux/Mac
    # venv\Scripts\activate     # Windows
    ```
3.  **Install Dependencies:**
    ```bash
    pip install -r requirements.txt
    ```
4.  **Run Django Application:**
    ```bash
    python manage.py runserver
    ```
    Access the application locally at: **👉 http://127.0.0.1:8000/}**

---

## ⚖️ Constraints and Limitations

### Constraints (Current Scope)

* **CSX-01:** Detection is limited to **URLs and email content** only.
* **CSX-02:** Deployment is restricted to **AWS Free Tier resources** ($\text{t3.micro}$).
* **CSX-03:** File upload size limit is **$\le 10 \text{MB}$**; Excel limit is **$\le 500$ rows**.
* **CSX-04:** **English language** support only for NLP module.

### Limitations (Future Scope)

* ❌ No real-time integration with email service providers ($\text{Gmail/Outlook}$).
* ❌ No malware, ransomware, or deep-level threat scanning.
* ❌ Current NLP models do not utilize $\text{BERT}$-based deep learning.

---

## 🚀 Future Enhancements

The following roadmap is planned to elevate CyberSentinelX to an enterprise-grade solution:

* **Advanced NLP:** Integrate **$\text{BERT}$/Transformer-based models** for superior contextual analysis.
* **Live Monitoring:** Implement **OAuth/IMAP** support for **live inbox monitoring** and scanning.
* **Extended Reach:** Develop **Chrome/Firefox browser extensions** and a dedicated **mobile application ($\text{Android/iOS}$)**.
* **Enterprise Scaling:** Migrate deployment to a container orchestration platform (**$\text{EKS/ECS}$**).
* **Multi-Language Support:** Expand NLP capabilities to enable **multi-language phishing detection**.

---

## 📧 Contact

| Role | Information |
| :--- | :--- |
| **Developer** | R. Gajendran |
| **Institution** | Birla Institute of Technology & Science, Pilani |
| **Organization** | HCL Technologies, Bengaluru, Karnataka |
| **Email** | $\text{202117bh009@wilp.bits-pilani.ac.in}$ |
| **GitHub** | $\text{Gaje1109/CyberSentinelX}$ |

*CyberSentinelX bridges artificial intelligence, user awareness, and automation — transforming conventional phishing detection into proactive, resilient digital defense.*