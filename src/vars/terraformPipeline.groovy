def call(Map config = [:]) {

    pipeline {
        agent any

        environment {
            AWS_DEFAULT_REGION = 'af-south-1'
            TF_IN_AUTOMATION   = 'true'
        }

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Authenticate to AWS') {
                steps {
                    dir(config.workingDir ?: 'src') {
                        withCredentials([[
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: config.awsCredentialsId ?: 'aws-credentials'
                        ]]) {
                            sh '''
                                echo "Authenticated to AWS"
                                aws sts get-caller-identity
                            '''
                        }
                    }
                }
            }

            stage('Terraform Init') {
                steps {
                    dir(config.workingDir ?: 'src') {
                        withCredentials([[
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: config.awsCredentialsId ?: 'aws-credentials'
                        ]]) {
                            sh '''
                                terraform --version
                                terraform init -input=false
                            '''
                        }
                    }
                }
            }

            stage('Terraform Plan') {
                steps {
                    dir(config.workingDir ?: 'src') {
                        withCredentials([[
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: config.awsCredentialsId ?: 'aws-credentials'
                        ]]) {
                            sh '''
                                terraform plan -out=tfplan
                                terraform plan -generate-config-out=generated.tf
                            '''
                        }
                    }
                }
            }

            stage('Terraform Apply') {
                when {
                    expression { return config.autoApply == true }
                }
                steps {
                    dir(config.workingDir ?: 'src') {
                        withCredentials([[
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: config.awsCredentialsId ?: 'aws-credentials'
                        ]]) {
                            sh '''
                                terraform apply -auto-approve tfplan
                            '''
                        }
                    }
                }
            }

            stage('Manual Approval Apply') {
                when {
                    expression { return config.autoApply != true }
                }
                steps {
                    input message: 'Approve Terraform Apply?', ok: 'Apply'

                    dir(config.workingDir ?: 'src') {
                        withCredentials([[
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: config.awsCredentialsId ?: 'aws-credentials'
                        ]]) {
                            sh '''
                                terraform apply tfplan
                            '''
                        }
                    }
                }
            }
stage('Commit and Push generated.tf') {
    steps {
        withCredentials([usernamePassword(credentialsId: 'git-creds', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh '''
                # Configure Git user (Required for commits)
                git config user.name "Jenkins CI"
                git config user.email "jenkins@yourdomain.com"
                
                # Add your remote URL using your credentials
                git remote set-url origin https://${GIT_USERNAME}:${GIT_PASSWORD}@://github.com

                # Stage the generated file
                git add generated.tf

                # Commit only if there are changes to avoid empty commit errors
                git diff --staged --quiet || git commit -m "Automated update of generated.tf [skip ci]"

                # Push the changes back to the remote repository (ensure you are on the correct branch)
                git push origin HEAD:${BRANCH_NAME}
            '''
        }
    }
}

        }

        post {
            always {
                echo 'Cleaning workspace...'
                cleanWs()
            }
            success {
                echo 'Terraform deployment completed successfully.'
            }
            failure {
                echo 'Terraform deployment failed.'
            }
        }
    }
}
