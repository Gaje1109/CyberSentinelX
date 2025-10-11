/**
 * CyberSentinelX: Declarative groovy file for a Hybrid Python-Java Application
 */
pipeline {
  agent any

  environment {
    // App Configuration
    APP_NAME           = "cybersentinelx"
    IMAGE_TAG          = "latest"
    JAVA_MODULE_PATH   = "urlExcelScanner"
    GIT_REPO_URL       = "https://github.com/Gaje1109/CyberSentinelX.git"
    GIT_BRANCH         = "main"

    // AWS Configuration
    S3_BUCKET_NAME   = "cybersentinelx-bits-capstone"
    AWS_REGION       = "ap-south-1"
    S3_MODELS_FOLDER = "artifactory"
    S3_DEPLOY_FOLDER = "cybersentinelx-docker-tar"

    // Jenkins Credentials IDs
    AWS_ACCESS_KEY_ID_CRED = "AWS_USERNAME"
    AWS_SECRET_KEY_CRED    = "AWS_SECRETKEY"
    EC2_CREDENTIALS_ID     = "cybersentinelx-ec2-ssh-key"

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
        git branch: "${GIT_BRANCH}", url: "${GIT_REPO_URL}"
        echo 'CyberSentinelX: Git checkout -- ends'
      }
    }

    stage('Build Java Component') {
      steps {
        echo 'CyberSentinelX: Build Java Component -- starts'
        dir("${JAVA_MODULE_PATH}") {
          script {
            if (isUnix()) {
              def rc = sh(script: "mvn clean install -DskipTests", returnStatus: true)
              if (rc != 0) error "Maven build failed with exit code ${rc}"
            } else {
              def rc = bat(script: "mvn clean install -DskipTests", returnStatus: true)
              if (rc != 0) error "Maven build failed with exit code ${rc}"
            }
          }
        }
      }
    }

    stage('Download Models and Data') {
      steps {
        script {
          echo 'CyberSentinelX: Download Models and Data -- starts'
          def assetDirs = ["emailScanner/eclassifier/model", "artifacts"]

          withCredentials([
            usernamePassword(credentialsId: env.AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED1'),
            usernamePassword(credentialsId: env.AWS_SECRET_KEY_CRED,    usernameVariable: 'UNUSED2',                 passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
          ]) {
            withEnv([
              "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
              "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
              "AWS_DEFAULT_REGION=${AWS_REGION}"
            ]) {
              assetDirs.each { dirPath ->
                echo "Syncing s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ -> ${dirPath}/"
                if (isUnix()) {
                  sh "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ ./${dirPath}/ || true"
                } else {
                  bat "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ .\\${dirPath}\\"
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
          if (isUnix()) {
            def rc = sh(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
            if (rc != 0) error "Docker build failed with exit code ${rc}"
          } else {
            def rc = bat(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
            if (rc != 0) error "Docker build failed with exit code ${rc}"
          }
        }
      }
    }

    stage('Push Artifacts and Version to S3') {
      steps {
        script {
          def imageTarFile = "${APP_NAME}.tar"
          def versionFile  = "latest-version.txt"
          writeFile file: versionFile, text: "${env.BUILD_NUMBER}"

          echo 'CyberSentinelX: Push Artifacts and Version to S3 -- starts'
          if (isUnix()) {
            def rc = sh(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
            if (rc != 0) error "docker save failed with exit code ${rc}"
          } else {
            def rc = bat(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
            if (rc != 0) error "docker save failed with exit code ${rc}"
          }

          if (!fileExists(imageTarFile)) error "CRITICAL: ${imageTarFile} not found after docker save"

          withCredentials([
            usernamePassword(credentialsId: env.AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED1'),
            usernamePassword(credentialsId: env.AWS_SECRET_KEY_CRED,    usernameVariable: 'UNUSED2',                 passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
          ]) {
            withEnv([
              "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
              "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
              "AWS_DEFAULT_REGION=${AWS_REGION}"
            ]) {
              // Upload artifacts
              if (isUnix()) {
                sh "aws s3 cp ${imageTarFile}   s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                sh "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                sh "aws s3 cp .env              s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                sh "aws s3 cp ${versionFile}    s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                sh "aws s3 cp DockerScripts/docker_deploy_app.sh s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
              } else {
                bat "aws s3 cp ${imageTarFile}    s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                bat "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                bat "aws s3 cp .env               s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                bat "aws s3 cp ${versionFile}     s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
                bat "aws s3 cp DockerScripts/docker_deploy_app.sh s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/"
              }
            }
          }
        }
      }
    }

    stage('Deploy to EC2') {
      steps {
        echo "Deploying build #${env.BUILD_NUMBER} to EC2 via S3 artifacts..."
        withCredentials([sshUserPrivateKey(credentialsId: EC2_CREDENTIALS_ID, keyFileVariable: 'SSH_KEY_FILE')]) {
          script {
            // Compose a remote bash script; escape double quotes for Windows ssh
            def remote = """
              set -e
              mkdir -p ${DEPLOY_DIR}
              cd ${DEPLOY_DIR}

              echo "[EC2] Pulling deployables from S3..."
              aws s3 cp s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/latest-version.txt .
              aws s3 cp s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/docker-compose.yml .
              aws s3 cp s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/.env .
              aws s3 cp s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/${APP_NAME}.tar .
              aws s3 cp s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/docker_deploy_app.sh .

              chmod +x docker_deploy_app.sh

              echo "[EC2] Loading Docker image..."
              docker load -i ${APP_NAME}.tar

              echo "[EC2] Starting with docker compose..."
              docker compose --env-file .env -f docker-compose.yml up -d --remove-orphans

              echo "[EC2] (Optional) run additional deploy steps script"
              ./docker_deploy_app.sh || true

              echo "[EC2] Cleanup old images..."
              docker image prune -f

              echo "[EC2] Done."
            """.trim()

            bat """
              ssh -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" %EC2_USER%@%EC2_HOST% "${remote.replace('"','\\\\"')}"
            """
          }
        }
      }
    }
  } // end stages

  post {
    always {
      echo 'CyberSentinelX: CleanUp -- starts'
      script {
        if (isUnix()) {
          sh "rm -f ${APP_NAME}.tar || true"
          sh "rm -f latest-version.txt || true"
        } else {
          bat "if exist ${APP_NAME}.tar del /f /q ${APP_NAME}.tar"
          bat "if exist latest-version.txt del /f /q latest-version.txt"
        }
      }
    }
    success {
      echo "CyberSentinelX: Pipeline finished successfully! Artifacts for build #${env.BUILD_NUMBER} pushed to S3 and deployed to EC2."
    }
    failure {
      echo "CyberSentinelX: Pipeline failed. Please check the logs above for details."
    }
  }
}
