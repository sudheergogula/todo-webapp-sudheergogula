#!/usr/bin/env groovy

pipeline {
    agent any

    environment {
        userName = "sudheergogula"
        imageName = "gogulasudheer/i-${userName}-${env.BRANCH_NAME}"
        k8s_namespace = "kubernetes-cluster-${userName}"
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
                    try {
                        echo "Initiating Kubernetes deployment ..."
                        sh "kubectl apply -k k8s/overlay/${env.BRANCH_NAME} -n ${k8s_namespace}"

                        echo "Verifying deployment ..."
                        def status = sh(script: "kubectl rollout status deployment/${env.BRANCH_NAME}-todo-webapp-deployment -n ${k8s_namespace} --watch --timeout=2m", returnStdout: true)
                        def exitCode = sh(script: 'echo $?', returnStdout: true)
                        if (exitCode) {
                            echo "Kubernetes deployment is successful with status - ${status}"
                        } else {
                            error "Pipeline aborted due to kubernetes deployment error - ${status}"
                        }
                    } catch (err) {
                        error "Pipeline aborted due to kubernetes deployment error - ${err}"
                    }
                }
            }
        }

        stage('Application Health check') {
            steps {
                // Wait for sometime for the load balancer IP to be allocated
                sleep(10)
                script {
                    try {
                        echo "Application health check ..."
                        timeout(2) {
                            waitUntil {
                                def ip = sh(script: "kubectl get service/${env.BRANCH_NAME}-todo-webapp-lb-service -n ${k8s_namespace} --output jsonpath='{.status.loadBalancer.ingress[0].ip}'", returnStdout: true)
                                if (ip != null && ip?.trim()) {
                                    echo "The load balancer IP is '${ip}'"
                                    def httpResponseCode = sh(script: "curl -s -o /dev/null -w '%{http_code}' ${ip} --connect-timeout 60", returnStdout: true)
                                    if (httpResponseCode != null && httpResponseCode?.trim() && httpResponseCode == "200") {
                                        echo "Application health check successful"
                                    } else {
                                        error "Application health check failed"
                                    }
                                    return true
                                } else {
                                    echo "Load balancer IP is not allocated yet"
                                    return false
                                }
                            }
                        }
                    } catch (err) {
                        echo "Error in doing App health check - ${err}"
                    }
                }
            }
        }
    }
    post {
        failure {
            echo "Pipeline failed"
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