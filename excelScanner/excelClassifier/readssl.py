import subprocess
import os
import glob
import json

jar_name = "CyberSentinelX-0.0.1-SNAPSHOT.jar"

# For local
JAR_PATH = "D:/Gajendran/Python Virtual Environments/CyberSentinelX/artifacts/CyberSentinelX-0.0.1-SNAPSHOT.jar"
# For Docker
#JAR_PATH = os.getenv('JAVA_JAR_PATH')
## Commented for Docker
#maven_project_path = "D:/Gajendran/Python Virtual Environments/CyberSentinelX/urlExcelScanner"
# BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
# target_dir = os.path.join(BASE_DIR, "artifacts")
## Find the build JAR in target/folder
#target_dir = os.path.join(maven_project_path, "target")
sMethodsName  =""

##################################################################################################################
# Method to call the Java ReadWriteURLSSL Method
def call_read_ssl(input_file):
    sMethodsName = "readssl.call_read_ssl()"
    main_class = "com.wilp.bits.url.ReadWriteURLSSL"

    if not JAR_PATH or not os.path.exists(JAR_PATH):
        raise FileNotFoundError(
            f"Java JAR path not found or invalid. Ensure the JAVA_JAR_PATH environment variable is set correctly. Current value: '{JAR_PATH}'")

    result = subprocess.run(
        ["java", "-cp", JAR_PATH, main_class, input_file],
        capture_output=True,
        text=True
    )
    if result.returncode != 0:
        print(f"{sMethodsName} ------- STDERR:", result.stderr)
        raise RuntimeError("Java failed")

    output_file = None
    for line in result.stdout.splitlines():
        line = line.strip()
        if line.startswith("{") and "output_path" in line:
            payload = json.loads(line)
            output_file = payload["output_path"]

    if output_file:
        print(f"Java returned output file: {output_file}")
        return output_file
    else:
        print(f"No file path found in Java output : {sMethodsName}")
        print(f"{sMethodsName} ------ STDOUT:", result.stdout)
        return None

##################################################################################################################

# Method to Call Email send functionality
def sendemail(output_file,email):
    sMethodsName = "readssl.sendemail()"
    # Email configurations
    email_class = "com.wilp.bits.email.EmailManagement"
    args = [output_file,email]
    print(args)
    if not JAR_PATH or not os.path.exists(JAR_PATH):
        raise FileNotFoundError(
            f"Java JAR path not found or invalid. Ensure the JAVA_JAR_PATH environment variable is set correctly. Current value: '{JAR_PATH}'")

    print(f"Found JAR: {JAR_PATH}")

    ## Run the Java class from the JAR
    result = subprocess.run(
        ["java", "-cp", JAR_PATH, email_class] + args,
        capture_output=True,
        text=True
    )
    print(f"{sMethodsName} ------------------ STDOUT:", result.stdout)
    print(f"{sMethodsName} ------------------ STDERR:", result.stderr)
##################################################################################################################