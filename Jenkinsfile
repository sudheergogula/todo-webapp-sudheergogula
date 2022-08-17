#!/usr/bin/env groovy

pipeline {
    agent any

    environment {
        imageName = "gogulasudheer/i-sudheergogula-${env.BRANCH_NAME}"
        k8s_namespace = 'kubernetes-cluster-sudheergogula'
    }

    tools {
        git 'Default'
        maven 'Maven3'
    }
    options {
        timestamps()

        timeout(time: 1, unit: 'HOURS')

        buildDiscarder(logRotator(daysToKeepStr: '10', numToKeepStr: '20'))
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Test case execution') {
            when {
                branch 'master'
            }
            steps {
                sh 'mvn test'
            }
        }
        stage('Sonarqube Analysis') {
            when {
                branch 'develop'
            }
            steps {
                withSonarQubeEnv(installationName: 'SonarQubeScanner') {
                    sh 'mvn test sonar:sonar'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ..."
                sh "docker build -t ${imageName}:latest -t ${imageName}:${BUILD_NUMBER} ."
            }
        }

        stage('Kubernetes Deployment') {
            steps {
                script {
                    echo "Initiating Kubernetes deployment ..."
                    sh "kubectl apply -k k8s/overlay/${env.BRANCH_NAME} -n ${k8s_namespace}"

                    echo "Verifying deployment ..."
                    def status = sh(script: "kubectl rollout status deployment/develop-todo-webapp-deployment -n ${k8s_namespace}", returnStdout: true)
                    def exitCode = sh(script: 'echo $?', returnStdout: true)
                    if (exitCode) {
                        echo "Kubernetes deployment is successful"
                    } else {
                        error "Pipeline aborted due to kubernetes deployment error - ${status}"
                    }
                }
            }
        }
    }
    post {
        failure {
            echo "Pipeline failed."
            // Uncomment the following lines to send a mail using emailext plugin
            // emailext to: 'sudheer.gogula@nagarro.com',
            // subject: "Jenkins build:${currentBuild.currentResult}: ${env.JOB_NAME}",
            // body: "${currentBuild.currentResult}: Job ${env.JOB_NAME}\nMore Info can be found here: ${env.BUILD_URL}",
            // attachLog: true
        }
        cleanup {
            sh "docker image rm ${imageName}:latest ${imageName}:${BUILD_NUMBER}"
            cleanWs()
        }
    }
}