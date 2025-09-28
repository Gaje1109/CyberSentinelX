import subprocess
import os
import glob
import json

jar_name = "CyberSentinelX-0.0.1-SNAPSHOT.jar"
maven_project_path = "D:/Gajendran/Python Virtual Environments/CyberSentinelX/urlExcelScanner"
## Find the build JAR in target/folder
target_dir = os.path.join(maven_project_path, "target")
sMethodsName  =""

##################################################################################################################
# Method to call the Java ReadWriteURLSSL Method
def call_read_ssl(input_file):
    sMethodsName = "readssl.call_read_ssl()"
    main_class = "com.wilp.bits.url.ReadWriteURLSSL"

    jar_files = glob.glob(os.path.join(target_dir,jar_name ))
    jar_files = [f for f in jar_files if not (f.endswith("-sources.jar") or f.endswith("-javadoc.jar"))]
    if not jar_files:
        raise FileNotFoundError(f"No JAR file found in target directory: {sMethodsName}")
    jar_path = jar_files[0]

    result = subprocess.run(
        ["java", "-cp", jar_path, main_class, input_file],
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
    jar_files = glob.glob(os.path.join(target_dir, jar_name))
    ## Exclude sources.jar and -javadoc.jar
    jar_files = [f for f in jar_files if not (f.endswith("-sources.jar") or f.endswith("-javadoc.jar"))]

    ## Check if jar file exist
    if not jar_files:
        raise FileNotFoundError(f"No JAR file found in target directory: {sMethodsName}")

    jar_path = jar_files[0]  # Take the first matching JAR
    print(f"Found JAR: {jar_path}")

    ## Run the Java class from the JAR
    result = subprocess.run(
        ["java", "-cp", jar_path, email_class] + args,
        capture_output=True,
        text=True
    )
    print(f"{sMethodsName} ------------------ STDOUT:", result.stdout)
    print(f"{sMethodsName} ------------------ STDERR:", result.stderr)
##################################################################################################################