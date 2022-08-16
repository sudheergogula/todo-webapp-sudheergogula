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
                    sh 'docker image prune -a -f'
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
        success {
            mail(to: 'sudheer.gogula@nagarro.com',
                    subject: "${BUILD_DISPLAY_NAME} pipeline ran successfully",
                    body: "Build no. ${BUILD_NUMBER} of branch ${env.BRANCH_NAME} executed successfully.")
        }
        failure {
            mail(to: 'sudheer.gogula@nagarro.com',
                    subject: "${BUILD_DISPLAY_NAME} pipeline failed",
                    body: "Build no. ${BUILD_NUMBER} of branch ${env.BRANCH_NAME} failed.")
        }
        cleanup {
            cleanWs()
        }
    }
}