# 🛡️ CyberSentinelX  
### *An AI-Driven Platform for Intelligent Phishing Detection and Threat Prevention*  

![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)  
![Django](https://img.shields.io/badge/Django-Framework-success.svg)  
![AWS](https://img.shields.io/badge/AWS-Cloud-orange.svg)  
![Terraform](https://img.shields.io/badge/Terraform-IaC-blueviolet.svg)  
![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-red.svg)  
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED.svg)  
![License](https://img.shields.io/badge/License-MIT-green.svg)  

---

## 📚 Table of Contents  
1. [📘 Overview](#-overview)  
2. [🧠 Key Features](#-key-features)  
3. [⚙️ System Architecture](#️-system-architecture)  
4. [🧰 Installation & Setup](#-installation--setup)  
5. [☁️ Deployment (AWS + Jenkins + Terraform)](#️-deployment-aws--jenkins--terraform)  
6. [🧪 Testing & Validation](#-testing--validation)  
7. [🏆 Achievements](#-achievements)  
8. [🚫 Limitations](#-limitations)  
9. [🚀 Future Enhancements](#-future-enhancements)  
10. [📚 References](#-references)  
11. [📧 Contact](#-contact)  

---

## 📘 Overview  

**CyberSentinelX** is an **AI-driven cybersecurity platform** built to detect and prevent phishing and scam attacks.  
It combines **Machine Learning (ML)** and **Natural Language Processing (NLP)** for real-time phishing detection and user education.  

The system features two core detection modules — **URLScanner** and **EmailScanner** — integrated within a **Django web app**, deployed through **AWS, Terraform, Jenkins, and Docker**.  

---

## 🧠 Key Features  

### 🔍 URLScanner  
- Detects phishing URLs using **lexical and domain-based** features.  
- Trained using **Random Forest classifier**.  
- Supports both single URL and **bulk Excel file uploads**.  

### ✉️ EmailScanner  
- Classifies email content or uploaded PDFs as **Scam** or **Ham** using **TF-IDF** and **NLP** techniques.  
- Provides real-time classification results.  

### 📊 ExcelScanner  
- Processes bulk URLs from uploaded Excel files.  
- Automatically validates links and emails results to users.  

### 📘 Learn Module  
- Educates users on phishing prevention and best practices.  

### 🔁 Feedback Loop  
- Collects user feedback for **model retraining** and improvement.  

### ☁️ DevOps Integration  
- End-to-end **CI/CD** automation with **Jenkins**.  
- Infrastructure provisioning using **Terraform (IaC)**.  
- Containerized deployment via **Docker** on **AWS (EC2, S3, Lambda, SSM)**.  

---

## ⚙️ System Architecture  

### 🧩 High-Level Design
