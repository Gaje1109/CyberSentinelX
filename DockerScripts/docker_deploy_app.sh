#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# --- Configuration ---
S3_BUCKET="cybersentinelx-bits-capstone/cybersentinelx-docker-tar"
APP_NAME="cybersentinelx"
# DEPLOY_DIR is now in a location ssm-user can access
DEPLOY_DIR="/home/ubuntu/CyberSentinelX_Dockerized"
VERSION_FILE_LOCAL="$DEPLOY_DIR/current-version.txt"
LOG_FILE="/home/ubuntu/CyberSentinelX_Dockerized/deployment.log"
# --- End Configuration ---

# Redirect all output to a log file
exec &>> $LOG_FILE

echo "---"
echo "Deployment script started at $(date)"

# Navigate to the deployment directory
cd "$DEPLOY_DIR"

# Get the latest version number from S3
echo "Checking for new version in s3://${S3_BUCKET}/latest-version.txt..."
aws s3 cp "s3://${S3_BUCKET}/latest-version.txt" "latest-version.txt.tmp" --region ap-south-1
LATEST_VERSION=$(cat latest-version.txt.tmp)

# Get the currently deployed version number, if it exists
CURRENT_VERSION=$(cat "$VERSION_FILE_LOCAL" 2>/dev/null || echo "0")

echo "Latest version in S3: $LATEST_VERSION"
echo "Currently deployed version: $CURRENT_VERSION"

# If the latest version is different from the current version, deploy
if [ "$LATEST_VERSION" != "$CURRENT_VERSION" ]; then
    echo "New version detected. Starting deployment of version $LATEST_VERSION..."

    # Download the new artifacts
    echo "Pulling artifacts from S3..."
    aws s3 cp "s3://${S3_BUCKET}/${APP_NAME}.tar" . --region ap-south-1
    aws s3 cp "s3://${S3_BUCKET}/docker-compose.yml" . --region ap-south-1
    aws s3 cp "s3://${S3_BUCKET}/.env" . --region ap-south-1

    # Deploy using Docker Compose with sudo
    echo "Loading Docker image..."
    sudo docker load -i "$APP_NAME.tar"

    echo "Stopping and starting services with Docker Compose..."
    sudo docker compose down || true
    sudo docker compose up -d

    # Clean up and update the local version file
    echo "Cleaning up artifacts..."
    rm "$APP_NAME.tar"
    mv latest-version.txt.tmp "$VERSION_FILE_LOCAL"

    echo "Deployment of version $LATEST_VERSION complete."
else
    echo "No new version detected. Nothing to do."
    rm latest-version.txt.tmp
fi