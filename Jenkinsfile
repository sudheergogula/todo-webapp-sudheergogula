// noinspection GroovyAssignabilityCheck

pipeline {
    agent any

    environment {
        registry = "gogulasudheer/i-todo-sudheergogula-${env.BRANCH_NAME}"
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
                sh 'mvn clean compile'
            }
        }
        stage('Test case execution') {
            when {
                branch 'master'
            }
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Sonarqube Analysis') {
            when {
                branch 'develop'
            }
            steps {
                withSonarQubeEnv(installationName: 'SonarQubeScanner') {
                    sh 'mvn clean package sonar:sonar'
                }
            }
        }
        stage('Kubernetes Deployment') {
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

                echo "Initiating Kubernetes deployment ..."
            }
        }
    }
}