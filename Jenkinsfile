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
                    cp target/*.war target/practice_adh-${BUILD_NUMBER}.war
                '''
                archiveArtifacts artifacts: 'target/practice_adh-*.war', fingerprint: true
                echo 'Artifact Created Successfully!!'
            }
        }

        stage('Build & Tag Docker Image') {
            steps {
                sh "docker build -t sagardocker/practice_adh:latest -t practice_adh:latest ."
            }
        }

        stage('Docker Image Scanning') {
            steps {
                echo 'Scanning Docker Image with Trivy...'
                sh 'trivy image sagardocker/practice_adh:latest || echo "Scan Failed - Proceeding with Caution"'
                echo 'Docker Image Scanning Completed!'
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhubCred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                        sh "docker tag practice_adh:latest $DOCKER_USER/practice_adh:latest"
                        sh "docker push $DOCKER_USER/practice_adh:latest"
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

                            docker tag practice_adh:latest 251335054837.dkr.ecr.ap-south-1.amazonaws.com/sagardocker:practice_adh-latest
                            docker push 251335054837.dkr.ecr.ap-south-1.amazonaws.com/sagardocker:practice_adh-latest
                        '''
                        echo 'Docker Image Pushed to Amazon ECR Successfully!'
                    }
                }
            }
        }

        stage('Upload Docker Image to Harbor') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'harbor-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh '''
                    echo "$PASSWORD" | docker login 43.205.243.68:8082 -u "$USERNAME" --password-stdin
                    docker tag practice_adh:latest 43.205.243.68:8082/practice_adh/practice_adh:latest
                    docker push 43.205.243.68:8082/practice_adh/practice_adh:latest
                    docker logout 43.205.243.68:8082
                    '''
                    }
                }
            }
        }

        stage('Clean Up Local Docker Images') {
            steps {
                echo 'Cleaning Up Local Docker Images...'
                sh '''
                docker rmi sagardocker/practice_adh:latest || echo "Image not found or already deleted"
                docker rmi practice_adh:latest || echo "Image not found or already deleted"
                docker rmi 251335054837.dkr.ecr.ap-south-1.amazonaws.com/sagardocker:practice_adh-latest || echo "Image not found or already deleted"
                docker rmi 43.205.243.68:8082/practice-docker-aws-harboor/practice-docker-aws-harboor:latest || echo "Image not found or already deleted"
                docker image prune -f
                '''
                echo 'Local Docker Images Cleaned Up Successfully!!'
            }
        }
    }
}