import subprocess
import os
import glob
import jpype
import jpype.imports
from jpype.types import JByte

maven_project_path = "D:/Gajendran/Python Virtual Environments/CyberSentinelX/urlExcelScanner"
## Find the build JAR in target/folder
target_dir = os.path.join(maven_project_path, "target")



def call_read_ssl_jvm(input_file):
    # One-time: start JVM with your JAR on classpath
    jar_path = "D:/Gajendran/Python Virtual Environments/CyberSentinelX/urlExcelScanner/target/CyberSentinelX-0.0.1-SNAPSHOT.jar"
    if not jpype.isJVMStarted():
        jpype.startJVM(classpath=[jar_path])

    # Import your class
    from com.wilp.bits.url import ReadWriteURLSSL

    # 1) Call a STATIC method that returns a String path
    #    e.g., public static String process(String inputPath)
    # out_path = ReadWriteURLSSL.excelReadAndCheck(input_file)
    # print("Output path:", str(out_path))

    # 2) Call an INSTANCE method that returns java.io.File
    #    e.g., public File excelReadAndCheck(String inputPath)
    obj = ReadWriteURLSSL()
    file_obj = obj.excelReadAndCheck(input_file)
    print("File path:", str(file_obj.getAbsolutePath()))

    # 3) If a method returns byte[] (recommended for direct transfer)
    #    e.g., public static byte[] processToBytes(byte[] in)
    #    Convert Python bytes -> Java byte[] and back:
    # py_bytes = open("/data/input.xlsx", "rb").read()
    # JArrayByte = jpype.JArray(JByte)
    # java_in = JArrayByte(py_bytes)
    # java_out = ReadWriteURLSSL.processToBytes(java_in)  # returns byte[]
    # py_out = bytes(java_out)  # Python bytes
    # with open("/data/output.xlsx", "wb") as f:
    #     f.write(py_out)

    # Optional: stop JVM when your app exits
    # jpype.shutdownJVM()


def call_read_ssl(input_file):
    java_folder = "urlvalidator"
    # /src/main/java/com/wilp/bits/email
    #maven_project_path = "D:/Gajendran/Python Virtual Environments/CyberSentinelX/urlExcelScanner"
    #input_file = "C:/Users/User/Downloads/MAH_20240121_153659_1.xlsx"
    move_to_file = ""
    main_class = "com.wilp.bits.url.ReadWriteURLSSL.excelReadAndCheck"
    args = [input_file]
    maven_exe = r"D:\\Gajendran\\Softwares\\Java\\apache-maven-3.9.11-bin\\apache-maven-3.9.11\\bin\\mvn.cmd"
    ## Run Maven package to build the JAR
    ##subprocess.run([maven_exe, "clean", "package"], cwd = maven_project_path, check = True)

    jar_files = glob.glob(os.path.join(target_dir, "CyberSentinelX-0.0.1-SNAPSHOT.jar"))
    ## Exclude sources.jar and -javadoc.jar
    jar_files = [f for f in jar_files if not (f.endswith("-sources.jar") or f.endswith("-javadoc.jar"))]

    ## Check if jar file exist
    if not jar_files:
        raise FileNotFoundError("No JAR file found in target directory.")

    jar_path = jar_files[0]  # Take the first matching JAR
    print(f"Found JAR: {jar_path}")

    ## Run the Java class from the JAR
    result = subprocess.run(
        ["java", "-cp", jar_path, main_class] + args,
        capture_output=True,
        text=True
    )
    print("STDOUT:", result.stdout)
    print("STDERR:", result.stderr)

    output_file = None
    for line in result.stdout.splitlines():
        if line.startswith("OUTPUT_FILE="):
            output_file = line.replace("OUTPUT_FILE=","").strip()

    if output_file:
        print(f"Java returned output file : {output_file}")
        return output_file
    else:
        print("NO file")

def sendemail(output_file,email):
    # Email configurations
    email_class = "com.wilp.bits.email.EmailManagement"
    args = [output_file,email]
    print(args)
    jar_files = glob.glob(os.path.join(target_dir, "CyberSentinelX-0.0.1-SNAPSHOT.jar"))
    ## Exclude sources.jar and -javadoc.jar
    jar_files = [f for f in jar_files if not (f.endswith("-sources.jar") or f.endswith("-javadoc.jar"))]

    ## Check if jar file exist
    if not jar_files:
        raise FileNotFoundError("No JAR file found in target directory.")

    jar_path = jar_files[0]  # Take the first matching JAR
    print(f"Found JAR: {jar_path}")

    ## Run the Java class from the JAR
    result = subprocess.run(
        ["java", "-cp", jar_path, email_class] + args,
        capture_output=True,
        text=True
    )
    print("STDOUT:", result.stdout)
    print("STDERR:", result.stderr)