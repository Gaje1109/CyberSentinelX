/**
 * CyberSentinelX:  Declarative groovy file for a Hybrid Python-Java Application
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
    S3_BUCKET_NAME     = "cybersentinelx-bits-capstone"
    AWS_REGION         = "ap-south-1"
    S3_MODELS_FOLDER   = "artifactory"
    S3_DEPLOY_FOLDER   = "cybersentinelx-docker-tar"

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
        echo "Checking out branch '${GIT_BRANCH}' from ${GIT_REPO_URL}..."
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
              def mvnStatus = sh(script: "mvn clean install -DskipTests", returnStatus: true)
              if (mvnStatus != 0) error "Maven build failed with exit code ${mvnStatus}"
            } else {
              def mvnStatus = bat(script: "mvn clean install -DskipTests", returnStatus: true)
              if (mvnStatus != 0) error "Maven build failed with exit code ${mvnStatus}"
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
            usernamePassword(credentialsId: env.AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED_AWS_USER'),
            usernamePassword(credentialsId: env.AWS_SECRET_KEY_CRED,     usernameVariable: 'UNUSED_AWS_USER2',       passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
          ]) {
            withEnv([
              "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
              "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
              "AWS_DEFAULT_REGION=${AWS_REGION}"
            ]) {
              assetDirs.each { dirPath ->
                echo "Syncing s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ -> ${dirPath}/"
                if (isUnix()) {
                  def rc = sh(script: "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ ./${dirPath}/ || true", returnStatus: true)
                  if (rc != 0) echo "Warning: aws s3 sync returned ${rc} for ${dirPath} (check logs)."
                } else {
                  def rc = bat(script: "aws s3 sync s3://${S3_BUCKET_NAME}/${S3_MODELS_FOLDER}/${dirPath}/ .\\${dirPath}\\", returnStatus: true)
                  if (rc != 0) echo "Warning: aws s3 sync returned ${rc} for ${dirPath} (check logs)."
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
            def buildResult = sh(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
            if (buildResult != 0) error "Docker build failed with exit code ${buildResult}"
          } else {
            def buildResult = bat(script: "docker build -t ${APP_NAME}:${IMAGE_TAG} .", returnStatus: true)
            if (buildResult != 0) error "Docker build failed with exit code ${buildResult}"
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
            def saveResult = sh(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
            if (saveResult != 0) error "docker save failed with exit code ${saveResult}"
          } else {
            def saveResult = bat(script: "docker save ${APP_NAME}:${IMAGE_TAG} -o ${imageTarFile}", returnStatus: true)
            if (saveResult != 0) error "docker save failed with exit code ${saveResult}"
          }

          if (!fileExists(imageTarFile)) error "CRITICAL: ${imageTarFile} not found after docker save"

          withCredentials([
            usernamePassword(credentialsId: env.AWS_ACCESS_KEY_ID_CRED, usernameVariable: 'AWS_ACCESS_KEY_ID_VALUE', passwordVariable: 'UNUSED_AWS_USER'),
            usernamePassword(credentialsId: env.AWS_SECRET_KEY_CRED,     usernameVariable: 'UNUSED_AWS_USER2',       passwordVariable: 'AWS_SECRET_ACCESS_KEY_VALUE')
          ]) {
            withEnv([
              "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID_VALUE}",
              "AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY_VALUE}",
              "AWS_DEFAULT_REGION=${AWS_REGION}"
            ]) {
              if (isUnix()) {
                def rc1 = sh(script: "aws s3 cp ${imageTarFile}   s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                def rc2 = sh(script: "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                def rc3 = sh(script: "aws s3 cp .env              s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                def rc4 = sh(script: "aws s3 cp ${versionFile}    s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                if (rc1 != 0 || rc2 != 0 || rc3 != 0 || rc4 != 0) error "One or more aws s3 cp commands failed. Check the logs."
              } else {
                def rc1 = bat(script: "aws s3 cp ${imageTarFile}   s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                def rc2 = bat(script: "aws s3 cp docker-compose.yml s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                def rc3 = bat(script: "aws s3 cp .env               s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                def rc4 = bat(script: "aws s3 cp ${versionFile}     s3://${S3_BUCKET_NAME}/${S3_DEPLOY_FOLDER}/", returnStatus: true)
                if (rc1 != 0 || rc2 != 0 || rc3 != 0 || rc4 != 0) error "One or more aws s3 cp commands failed. Check the logs."
              }
            }
          }
        }
      }
    }

    stage('Deploy to EC2') {
      steps {
        echo "Deploying new version #${env.BUILD_NUMBER} to EC2 host: ${EC2_HOST}"
        withCredentials([sshUserPrivateKey(credentialsId: EC2_CREDENTIALS_ID, keyFileVariable: 'SSH_KEY_FILE')]) {
          script {
            // Script committed to Git and present in the workspace after 'git checkout'
            def LOCAL_DEPLOY_SCRIPT_WIN = "${WORKSPACE}\\DockerScripts\\docker_deploy_app.sh"
            def LOCAL_DEPLOY_SCRIPT_FWD = LOCAL_DEPLOY_SCRIPT_WIN.replace('\\','/')
            def REMOTE_DEPLOY_SCRIPT    = "${DEPLOY_DIR}/docker_deploy_app.sh"

            // 1) Verify script exists
            bat """
              echo WORKSPACE=%WORKSPACE%
              if not exist "${LOCAL_DEPLOY_SCRIPT_WIN}" (
                echo ERROR: File not found: ${LOCAL_DEPLOY_SCRIPT_WIN}
                dir /s /b DockerScripts\\*.sh
                exit /b 1
              )
            """

            // 2) Normalize CRLF -> LF to avoid bash^M on EC2
            bat """
              powershell -NoProfile -Command ^
                "$c = Get-Content -Raw \\"${LOCAL_DEPLOY_SCRIPT_WIN}\\"; ^
                 $c = $c -replace '\\r\\n','`n'; ^
                 Set-Content -NoNewline -Encoding Ascii \\"${LOCAL_DEPLOY_SCRIPT_WIN}\\" $c"
            """

            // 3) Ensure ssh/scp exist
            bat "where ssh"
            bat "where scp"

            // 4) Create remote dir, copy, chmod, run
            bat "ssh -o StrictHostKeyChecking=no -i \"%SSH_KEY_FILE%\" %EC2_USER%@%EC2_HOST% \"mkdir -p ${DEPLOY_DIR}\""

            bat """
              scp -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" ^
                "${LOCAL_DEPLOY_SCRIPT_FWD}" ^
                %EC2_USER%@%EC2_HOST%:${REMOTE_DEPLOY_SCRIPT}
            """

            bat """
              ssh -o StrictHostKeyChecking=no -i "%SSH_KEY_FILE%" ^
                %EC2_USER%@%EC2_HOST% ^
                "chmod +x ${REMOTE_DEPLOY_SCRIPT} && ${REMOTE_DEPLOY_SCRIPT} ${S3_BUCKET_NAME} ${S3_DEPLOY_FOLDER} ${DEPLOY_DIR}"
            """
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
