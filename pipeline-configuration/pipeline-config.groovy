/**
 * Declarative Jenkins Pipeline for a Hybrid Python/Django and Java Application
 *
 * This pipeline:
 * 1. Checks out code from SCM.
 * 2. Builds the Java component using Maven.
 * 3. Builds the final Docker image on the Jenkins agent.
 * 4. Archives the Docker image and transfers it to EC2 along with config files.
 * 5. Deploys the application on EC2 using Docker Compose for robust management.
 */
pipeline {
    agent any

    environment {
        // App Configuration
        APP_NAME = "cybersentinelx"
        IMAGE_TAG = "latest"
        JAVA_MODULE_PATH = "urlExcelScanner" // Your Java module directory

        // EC2 Target Configuration
        EC2_HOST = "3.108.196.241"
        EC2_USER = "ubuntu"
        DEPLOY_DIR = "/home/ubuntu/CyberSentinelX-Docker"
        EC2_CREDENTIALS_ID = "cybersentinelx-ec2-ssh-key" // Jenkins SSH credentials ID
    }

    stages {
        stage('Checkout Source Code') {
            steps {
                echo "Checking out source code from SCM..."
                // Uses SCM configured in the Jenkins Job for flexibility and security
                checkout scm
            }
        }

        stage('Build Java Component') {
            steps {
                echo "Building the Java component in '${JAVA_MODULE_PATH}'..."
                dir(JAVA_MODULE_PATH) {
                    // Assuming Jenkins agent is Linux-based. Use 'bat' for Windows.
                    sh 'mvn clean install'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image: ${APP_NAME}:${IMAGE_TAG}..."
                // This builds the image on the Jenkins agent.
                // The Dockerfile should be at the root of your repository.
                sh "docker build -t ${APP_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Push to EC2') {
            steps {
                script {
                    def imageTarFile = "${APP_NAME}.tar"
                    echo "Saving Docker image to ${imageTarFile}..."
                    sh "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}"

                    echo "Transferring files to EC2 host: ${EC2_HOST}..."
                    sshagent([EC2_CREDENTIALS_ID]) {
                        sh """
                            # Ensure the deployment directory exists on EC2
                            ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'mkdir -p ${DEPLOY_DIR}'

                            # Copy the Docker image, compose file, and environment file
                            scp -o StrictHostKeyChecking=no ${imageTarFile} ${EC2_USER}@${EC2_HOST}:${DEPLOY_DIR}/
                            scp -o StrictHostKeyChecking=no docker-compose.yml ${EC2_USER}@${EC2_HOST}:${DEPLOY_DIR}/
                            scp -o StrictHostKeyChecking=no .env ${EC2_USER}@${EC2_HOST}:${DEPLOY_DIR}/
                        """
                    }
                }
            }
        }

        stage('Deploy on EC2') {
            steps {
                echo "Deploying application on EC2 using Docker Compose..."
                sshagent([EC2_CREDENTIALS_ID]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                            cd ${DEPLOY_DIR}

                            echo "Loading Docker image from tar file..."
                            docker load -i ${APP_NAME}.tar

                            echo "Stopping existing services..."
                            docker compose down || true

                            echo "Starting new services with Docker Compose..."
                            docker compose up -d

                            echo "Cleaning up tar file on EC2..."
                            rm ${APP_NAME}.tar
                        '
                    """
                }
            }
        }
    }

    post {
        // This 'always' block ensures cleanup happens even if the pipeline fails
        always {
            echo "Cleaning up local workspace..."
            sh "rm -f ${APP_NAME}.tar"
        }
        success {
            echo "Pipeline finished successfully!"
            echo "Application should be running at: http://${EC2_HOST}" // Port 80 is default
        }
        failure {
            echo "Pipeline failed. Please check the logs."
        }
    }
}