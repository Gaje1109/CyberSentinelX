# Stage -1 :  JAVA

# We use a Maven image that includes the Java Development Kit (JDK)
FROM maven:3.8-openjdk-8 AS builder

# Set the working directory for the Java build
WORKDIR /usr/src/app

# Copy the Java source code into the builder stage
COPY ./urlExcelScanner/ .

# Run the Maven build command to compile the code and create the .jar file
RUN mvn clean install


# Stage -1 :  PYTHON
# specify base image for our application
FROM python:3.11-slim

# Set environment variables for Python
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# Install JRE
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates gnupg \
 && mkdir -p /usr/share/keyrings \
 && curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg \
 && echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb bookworm main" > /etc/apt/sources.list.d/adoptium.list \
 && apt-get update && apt-get install -y temurin-21-jre \
 && rm -rf /var/lib/apt/lists/*

# Set the working directory for the Django app
WORKDIR /app

# Python dependencies
COPY requirements.txt .

# Install the Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the .jar file from the builder stage ---
COPY --from=builder /usr/src/app/target/CyberSentinelX-0.0.1-SNAPSHOT.jar /app/java_processor/app.jar

# Copy all your project files
COPY . .

# Building Java Jar
# Put the JAR in a known path inside th image
#COPY artifacts/CyberSentinelX-0.0.1-SNAPSHOT.jar /opt/csx/urlExcelScanner.jar
# Copy JAR to /app/artifacts
COPY artifacts/CyberSentinelX-0.0.1-SNAPSHOT.jar /app/artifacts/


# Set environment variables for Django and Java
ENV DJANGO_SETTINGS_MODULE=cyberSniffer.settings
ENV JAVA_JAR_PATH=/app/java_processor/app.jar

# Expose the port the app runs on
EXPOSE 8000

CMD ["bash", "-lc", "gunicorn cyberSniffer.wsgi:application --bind 0.0.0.0:8000 --workers 3"]