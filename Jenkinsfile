// noinspection GroovyAssignabilityCheck

pipeline {
    agent any

    environment {
        registry = "gogulasudheer/i-sudheergogula-${env.BRANCH_NAME}"
        registryCredential = 'dockerhub-token'
    }

    tools {
        git 'Default'
        maven 'Maven3'
        dockerTool 'Docker'
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

        stage('Build and push docker image') {
            steps {
                echo "Building Docker image ..."
                script {
                    dockerImage = docker.build registry
                    docker.withRegistry('', registryCredential) {
                        dockerImage.push("${BUILD_NUMBER}")
                        dockerImage.push('latest')
                    }
                }
                echo "Docker image pushed to '${registry}'"
            }
            post {
                always {
                    sh 'docker image prune -a --filter="label=app=todowebapp"'
                }
            }
        }

        stage('Kubernetes Deployment') {
            steps {
                echo "Initiating Kubernetes deployment ..."
            }
        }
    }
    post {
        failure {
            emailext to: 'sudheer.gogula@nagarro.com',
            subject: "Jenkins build:${currentBuild.currentResult}: ${env.JOB_NAME}",
            body: "${currentBuild.currentResult}: Job ${env.JOB_NAME}\nMore Info can be found here: ${env.BUILD_URL}",
            attachLog: true
        }
        cleanup {
            cleanWs()
        }
    }
}