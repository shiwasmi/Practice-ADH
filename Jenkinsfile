pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    }

    tools {
        maven 'maven-3.9.16'
    }

    stages {
        stage('Code Compilation') {
            steps {
                echo 'Starting Code Compilation...'
                sh 'mvn clean compile'
                echo 'Code Compilation Completed Successfully!'
            }
        }

        stage('Code QA Execution') {
            steps {
                echo 'Running JUnit Test Cases...'
                sh 'mvn test'
                echo 'JUnit Test Cases Completed Successfully!'
            }
        }

        stage('Code Package') {
            steps {
                echo 'Creating Artifact...'
                sh 'mvn package'
                sh '''
                    # If WAR is expected
                    cp target/*.war target/practice_ADH-${BUILD_NUMBER}.war
                '''
                archiveArtifacts artifacts: 'target/practice_ADH-*.war', fingerprint: true
                echo 'Artifact Created Successfully!!'
            }
        }

        stage('Build & Tag Docker Image') {
            steps {
                sh "docker build -t sagardocker/practice_ADH:latest -t practice_ADH:latest ."
            }
        }

        stage('Docker Image Scanning') {
            steps {
                echo 'Scanning Docker Image with Trivy...'
                sh 'trivy image sagardocker/practice_ADH:latest || echo "Scan Failed - Proceeding with Caution"'
                echo 'Docker Image Scanning Completed!'
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhubCred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                        sh "docker tag practice_ADH:latest $DOCKER_USER/practice_ADH:latest"
                        sh "docker push $DOCKER_USER/practice_ADH:latest"
                    }
                }
            }
        }

        stage('Push Docker Image to Amazon ECR') {
            steps {
                script {
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'ecr-credentials']]) {
                        sh '''
                            aws ecr get-login-password --region ap-south-1 | \
                              docker login --username AWS --password-stdin 251335054837.dkr.ecr.ap-south-1.amazonaws.com

                            docker tag practice_ADH:latest 251335054837.dkr.ecr.ap-south-1.amazonaws.com/sagardocker:practice_ADH-latest
                            docker push 251335054837.dkr.ecr.ap-south-1.amazonaws.com/sagardocker:practice_ADH-latest
                        '''
                        echo 'Docker Image Pushed to Amazon ECR Successfully!'
                    }
                }
            }
        }

        stage('Clean Up Local Docker Images') {
            steps {
                echo 'Cleaning Up Local Docker Images...'
                sh '''
                docker rmi sagardocker/practice_ADH:latest || echo "Image not found or already deleted"
                docker rmi practice_ADH:latest || echo "Image not found or already deleted"
                docker rmi 251335054837.dkr.ecr.ap-south-1.amazonaws.com/sagardocker:practice_ADH-latest || echo "Image not found or already deleted"
                docker image prune -f
                '''
                echo 'Local Docker Images Cleaned Up Successfully!!'
            }
        }
    }
}