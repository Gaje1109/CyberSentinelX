# specify base image for our application
FROM python:3.11-slim

# Install JRE
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates gnupg \
 && mkdir -p /usr/share/keyrings \
 && curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg \
 && echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb bookworm main" > /etc/apt/sources.list.d/adoptium.list \
 && apt-get update && apt-get install -y temurin-21-jre \
 && rm -rf /var/lib/apt/lists/*


WORKDIR /app

# Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# APP code
COPY . .

# Building Java Jar
# Put the JAR in a known path inside th image
COPY artifacts/CyberSentinelX-0.0.1-SNAPSHOT.jar /opt/csx/urlExcelScanner.jar

# Django env basics
ENV DJANGO_SETTINGS_MODULE=cyberSentinelX.settings \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    JAVA_BIN=java \
    JAVA_OPTS="-Xms256m -Xmx1024m" \
    JAVA_JAR_PATH=/opt/csx/urlExcelScanner.jar

EXPOSE 8000
CMD ["bash", "-lc", "gunicorn cyberSentinelX.wsgi:application --bind 0.0.0.0:8000 --workers 3"]