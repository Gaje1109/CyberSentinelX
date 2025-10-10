/**
 * Declarative Jenkins Pipeline for a Hybrid Python/Django and Java Application
 *
 * S3-BASED DEPLOYMENT - NO 'PIPELINE: AWS STEPS' PLUGIN REQUIRED
 *
 * This version includes a diagnostic 'echo' statement to print the AWS Access Key
 * being used, helping to debug 'InvalidAccessKeyId' errors.
 */
pipeline {
    agent any

    environment {
        // App Configuration
        APP_NAME = "cybersentinelx"
        IMAGE_TAG = "latest"
        JAVA_MODULE_PATH = "urlExcelScanner"
        GIT_REPO_URL = "https://github.com/Gaje1109/CyberSentinelX.git"
        GIT_BRANCH = "main"

        // AWS Configuration
        S3_BUCKET_NAME = "cybersentinelx-bits-capstone"
        AWS_REGION = "ap-south-1"
        S3_MODELS_FOLDER = "artifactory"
        S3_DEPLOY_FOLDER= "cybersentinelx-docker-tar"
        // Jenkins Credentials IDs
        AWS_ACCESS_KEY_ID_CRED = "AWS_USERNAME"
        AWS_SECRET_KEY_CRED = "AWS_SECRETKEY"
        EC2_CREDENTIALS_ID = "cybersentinelx-ec2-ssh-key"

        // EC2 Target Configuration
        EC2_HOST = "13.126.16.47"
        EC2_USER = "ubuntu"
        DEPLOY_DIR = "/home/ubuntu/CyberSentinel_Dockerized"
    }

    stages {
        stage('Checkout Source Code') {
            steps {
                echo "Checking out branch '${GIT_BRANCH}' from ${GIT_REPO_URL}..."
                git branch: GIT_BRANCH, url: GIT_REPO_URL
            }
        }

        stage('Build Java Component') {
            steps {
                echo "Building the Java component..."
                dir(JAVA_MODULE_PATH) {
                    bat 'mvn clean install'
                }
            }
        }

        stage('Download Models and Data') {
            steps {
                script {
                    echo "Downloading all models and data files from S3..."
                    def asset_dirs_to_sync = [
                        "emailScanner/eclassifier/model"
                    ]
                    withCredentials([
                        usernamePassword(credentialsId: AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED_PASS_1'),
                        usernamePassword(credentialsId: AWS_SECRET_KEY_CRED,  usernameVariable: 'UNUSED_USER_2',    passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
                    ]) {
                        withEnv([
                            "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
                            "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
                            "AWS_DEFAULT_REGION=${AWS_REGION}"
                        ]) {
                            asset_dirs_to_sync.each { dirPath ->
                                echo "Syncing s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ to local workspace at ${dirPath}/"
                                bat "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ ${dirPath}/"
                            }
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image: ${APP_NAME}:${IMAGE_TAG}..."
                    // This block captures the exit code of the build command
                    def buildResult = bat(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
                    // If the exit code is not 0 (success), fail the pipeline here
                    if (buildResult != 0) {
                        error "Docker build failed with exit code ${buildResult}. Check the logs above for details."
                    }
                }
            }
        }

        stage('Push Artifacts and Version to S3') {
            steps {
                script {
                    def imageTarFile = "${APP_NAME}.tar"
                    def versionFile = "latest-version.txt"

                    writeFile file: versionFile, text: env.BUILD_NUMBER

                    echo "Saving Docker image to ${imageTarFile}..."
                    // Capture the exit code of the save command
                    def saveResult = bat(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
                    if (saveResult != 0) {
                        error "Docker save failed with exit code ${saveResult}. Check the logs above for details."
                    }

                    // Verify that the file was actually created before trying to use it
                    if (!fileExists(imageTarFile)) {
                        error "CRITICAL: The 'docker save' command reported success, but the output file '${imageTarFile}' was not found."
                    }

                    echo "Uploading artifacts to s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                    withCredentials([
                        usernamePassword(credentialsId: AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED_PASS_1'),
                        usernamePassword(credentialsId: AWS_SECRET_KEY_CRED,  usernameVariable: 'UNUSED_USER_2',    passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
                    ]) {
                        withEnv([
                            "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
                            "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
                            "AWS_DEFAULT_REGION=${AWS_REGION}"
                        ]) {
                            bat "aws s3 cp ${imageTarFile} s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                            bat "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                            bat "aws s3 cp .env s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                            bat "aws s3 cp ${versionFile} s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Cleaning up local workspace..."
            // These commands will now only run if the files were successfully created
            bat "del /f /q ${APP_NAME}.tar"
            bat "del /f /q latest-version.txt"
        }
        success {
            echo "Pipeline finished successfully! Artifacts for build #${env.BUILD_NUMBER} pushed to S3."
        }
        failure {
            echo "Pipeline failed. Please check the logs."
        }
    }
}