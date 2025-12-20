pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  name: lumi-ci
spec:
  containers:
  - name: gradle
    image: gradle:8.10.2-jdk21-alpine
    command: [ "cat" ]
    tty: true
  - name: docker
    image: docker:28.5.1-cli-alpine3.22
    command: [ "cat" ]
    tty: true
    volumeMounts:
    - name: docker-socket
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
'''
        }
    }

    environment {
        BACKEND_IMAGE_NAME  = 'amicitia/lumi-backend'
        FRONTEND_IMAGE_NAME = 'amicitia/lumi-frontend'
        PYTHON_IMAGE_NAME   = 'amicitia/lumi-python'

        DOCKER_CREDENTIALS_ID = 'dockerhub-cred'
        DISCORD_WEBHOOK_CREDENTIALS_ID = 'discord-webhook-lumi'
    }

    stages {

        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(
                        script: 'git diff --name-only HEAD~1 || true',
                        returnStdout: true
                    ).trim().split('\n')

                    env.SHOULD_BUILD_BACKEND  = changedFiles.any { it.startsWith('Backend/') } ? "true" : "false"
                    env.SHOULD_BUILD_FRONTEND = changedFiles.any { it.startsWith('Frontend/') } ? "true" : "false"
                    env.SHOULD_BUILD_PYTHON   = changedFiles.any { it.startsWith('Python/') } ? "true" : "false"
                }
            }
        }

        stage('Docker Login') {
            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: DOCKER_CREDENTIALS_ID,
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh '''
                            docker logout || true
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                        '''
                    }
                }
            }
        }

        stage('Backend Test') {
            when { expression { env.SHOULD_BUILD_BACKEND == "true" } }
            steps {
                container('gradle') {
                    sh '''
                        cd Backend
                        chmod +x gradlew
                        ./gradlew clean test
                    '''
                }
            }
        }

        stage('Backend Build & Push') {
            when { expression { env.SHOULD_BUILD_BACKEND == "true" } }
            steps {
                container('gradle') {
                    sh '''
                        cd Backend
                        chmod +x gradlew
                        ./gradlew clean build
                    '''
                }
                container('docker') {
                    sh '''
                        cd Backend
                        docker build -t $BACKEND_IMAGE_NAME:${BUILD_NUMBER} .
                        docker push $BACKEND_IMAGE_NAME:${BUILD_NUMBER}
                    '''
                }
            }
        }

        stage('Python Build & Push') {
            when { expression { env.SHOULD_BUILD_PYTHON == "true" } }
            steps {
                container('docker') {
                    sh '''
                        cd Python
                        docker build -t $PYTHON_IMAGE_NAME:${BUILD_NUMBER} .
                        docker push $PYTHON_IMAGE_NAME:${BUILD_NUMBER}
                    '''
                }
            }
        }

        stage('Frontend Build & Push') {
            when { expression { env.SHOULD_BUILD_FRONTEND == "true" } }
            steps {
                container('docker') {
                    sh '''
                        cd Frontend
                        docker build -t $FRONTEND_IMAGE_NAME:${BUILD_NUMBER} .
                        docker push $FRONTEND_IMAGE_NAME:${BUILD_NUMBER}
                    '''
                }
            }
        }

        stage('Trigger ArgoCD Sync') {
            steps {
                build job: 'lumi-manifests',
                    parameters: [
                        string(name: 'DOCKER_IMAGE_VERSION', value: "${BUILD_NUMBER}"),
                        string(name: 'DID_BUILD_API', value: "${env.SHOULD_BUILD_BACKEND}"),
                        string(name: 'DID_BUILD_FRONT', value: "${env.SHOULD_BUILD_FRONTEND}"),
                        string(name: 'DID_BUILD_PYTHON', value: "${env.SHOULD_BUILD_PYTHON}")
                    ],
                    wait: true
            }
        }
    }

    post {
        always {
            withCredentials([string(
                credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                discordSend description: """
                Job: ${env.JOB_NAME}
                Build: ${currentBuild.displayName}
                Result: ${currentBuild.currentResult}
                Duration: ${(currentBuild.duration / 1000).intValue()}s
                """,
                result: currentBuild.currentResult,
                title: "Lumi CI Pipeline",
                webhookURL: "${DISCORD_WEBHOOK_URL}"
            }
        }
    }
}
