/**
 * CyberSentinelX:  Declarative groovy file for a Hybrid Python-Java Application
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
        S3_DEPLOY_FOLDER = "cybersentinelx-docker-tar"

        // Jenkins Credentials IDs (replace with your IDs)
        AWS_ACCESS_KEY_ID_CRED = "AWS_USERNAME"
        AWS_SECRET_KEY_CRED  = "AWS_SECRETKEY"
        EC2_CREDENTIALS_ID   = "cybersentinelx-ec2-ssh-key"

        // EC2 Target Configuration
        EC2_HOST = "13.126.16.47"
        EC2_USER = "ubuntu"
        DEPLOY_DIR = "/home/ubuntu/CyberSentinel_Dockerized"
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout Source Code') {
            steps {
                echo 'CyberSentinelX: Git checkout -- starts'
                echo "Checking out branch '${GIT_BRANCH}' from ${GIT_REPO_URL}..."
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO_URL}"
                echo 'CyberSentinelX: Git checkout -- ends'
            }
        }

        stage('Build Java Component') {
            steps {
                echo 'CyberSentinelX: Build Java Component -- starts'
                echo "Building the Java component..."
                dir("${JAVA_MODULE_PATH}") {
                    script {
                        if (isUnix()) {
                            // Use mvn (ensure Maven is installed on agent)
                            def mvnStatus = sh(script: "mvn clean install -DskipTests", returnStatus: true)
                            if (mvnStatus != 0) {
                                error "Maven build failed with exit code ${mvnStatus}"
                            }
                        } else {
                            // Windows agent
                            def mvnStatus = bat(script: "mvn clean install -DskipTests", returnStatus: true)
                            if (mvnStatus != 0) {
                                error "Maven build failed with exit code ${mvnStatus}"
                            }
                        }
                    }
                }
            }
        }

        stage('Download Models and Data') {
            steps {
                script {
                    echo 'CyberSentinelX: Download Models and Data -- starts'
                    echo "Downloading models and data files from S3..."
                    def asset_dirs_to_sync = [
                        "emailScanner/eclassifier/model",
                        "artifacts"
                    ]

                    // Bind AWS credentials (usernamePassword is used here to map to two variables).
                    // Make sure your Jenkins credentials are set up with those IDs.
                    withCredentials([
                        usernamePassword(credentialsId: env.AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED_AWS_USER'),
                        usernamePassword(credentialsId: env.AWS_SECRET_KEY_CRED,  usernameVariable: 'UNUSED_AWS_USER2', passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
                    ]) {
                        withEnv([
                            "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
                            "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
                            "AWS_DEFAULT_REGION=${AWS_REGION}"
                        ]) {
                            asset_dirs_to_sync.each { dirPath ->
                                echo "Syncing s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ -> ${dirPath}/"
                                if (isUnix()) {
                                    def rc = sh(script: "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ ./${dirPath}/ || true", returnStatus: true)
                                    if (rc != 0) {
                                        echo "Warning: aws s3 sync returned ${rc} for ${dirPath} (check logs)."
                                    }
                                } else {
                                    def rc = bat(script: "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ .\\${dirPath}\\", returnStatus: true)
                                    if (rc != 0) {
                                        echo "Warning: aws s3 sync returned ${rc} for ${dirPath} (check logs)."
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo 'CyberSentinelX: Build Docker Image -- starts'
                    echo "Building Docker image: ${APP_NAME}:${IMAGE_TAG}..."
                    if (isUnix()) {
                        def buildResult = sh(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
                        if (buildResult != 0) {
                            error "Docker build failed with exit code ${buildResult}"
                        }
                    } else {
                        def buildResult = bat(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
                        if (buildResult != 0) {
                            error "Docker build failed with exit code ${buildResult}"
                        }
                    }
                }
            }
        }

        stage('Push Artifacts and Version to S3') {
            steps {
                script {
                    def imageTarFile = "${APP_NAME}.tar"
                    def versionFile = "latest-version.txt"
                    writeFile file: versionFile, text: "${env.BUILD_NUMBER}"
                    echo 'CyberSentinelX: Push Artifacts and Version to S3 -- starts'
                    echo "Saving Docker image to ${imageTarFile}..."
                    if (isUnix()) {
                        def saveResult = sh(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
                        if (saveResult != 0) {
                            error "docker save failed with exit code ${saveResult}"
                        }
                    } else {
                        def saveResult = bat(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
                        if (saveResult != 0) {
                            error "docker save failed with exit code ${saveResult}"
                        }
                    }

                    // Verify the tar file exists
                    if (!fileExists(imageTarFile)) {
                        error "CRITICAL: ${imageTarFile} not found after docker save"
                    }

                    echo "Uploading artifacts to s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                    withCredentials([
                        usernamePassword(credentialsId: env.AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED_AWS_USER'),
                        usernamePassword(credentialsId: env.AWS_SECRET_KEY_CRED,  usernameVariable: 'UNUSED_AWS_USER2', passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
                    ]) {
                        withEnv([
                            "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
                            "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
                            "AWS_DEFAULT_REGION=${AWS_REGION}"
                        ]) {
                            if (isUnix()) {
                                def rc1 = sh(script: "aws s3 cp ${imageTarFile} s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                def rc2 = sh(script: "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                def rc3 = sh(script: "aws s3 cp .env s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                def rc4 = sh(script: "aws s3 cp ${versionFile} s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                if (rc1 != 0 || rc2 != 0 || rc3 != 0 || rc4 != 0) {
                                    error "One or more aws s3 cp commands failed. Check the logs."
                                }
                            } else {
                                def rc1 = bat(script: "aws s3 cp ${imageTarFile} s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                def rc2 = bat(script: "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                def rc3 = bat(script: "aws s3 cp .env s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                def rc4 = bat(script: "aws s3 cp ${versionFile} s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                                if (rc1 != 0 || rc2 != 0 || rc3 != 0 || rc4 != 0) {
                                    error "One or more aws s3 cp commands failed. Check the logs."
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                echo 'CyberSentinelX: Deploy to EC2 -- starts'
                echo "Deploying new version #${env.BUILD_NUMBER} to EC2 host ${EC2_HOST}..."
                sshagent(credentials: [EC2_CREDENTIALS_ID]) {
                    // The 'sh' step works on both Windows and Linux agents for ssh/scp
                    script {
                        // 1. Copy the deployment script to the remote server
                        sh "scp -o StrictHostKeyChecking=no ./deploy.sh ${EC2_USER}@${EC2_HOST}:${DEPLOY_DIR}/deploy.sh"

                        // 2. SSH in, make the script executable, and then run it
                        sh "ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'chmod +x ${DEPLOY_DIR}/deploy.sh && ${DEPLOY_DIR}/deploy.sh'"
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'CyberSentinelX: CleanUp -- starts'
            echo "Cleaning up local workspace artifacts..."
            script {
                if (isUnix()) {
                    sh(script: "rm -f ${APP_NAME}.tar || true || echo 'no tar to delete'")
                    sh(script: "rm -f latest-version.txt || true || echo 'no version file to delete'")
                } else {
                    bat(script: "if exist ${APP_NAME}.tar del /f /q ${APP_NAME}.tar")
                    bat(script: "if exist latest-version.txt del /f /q latest-version.txt")
                }
            }
        }
        success {
            echo "CyberSentinelX: Pipeline finished successfully! Artifacts for build #${env.BUILD_NUMBER} pushed to S3."
        }
        failure {
            echo "CyberSentinelX: Pipeline failed. Please check the logs above for details."
        }
    }
}
