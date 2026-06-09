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
                    stage('Commit Generated Config') {
            steps {
                dir(config.workingDir ?: 'src') {
                    sh '''
                    git config user.name "Bhekanisphe"
                    git config user.email "bhekani.mdletsher@gmail.com"

                    if [ -f generated.tf ]; then
                    git add generated.tf
                    git commit -m "Add generated terraform config [ci skip]" || echo "No changes to commit"

                    git push https://github.com/Bhekanisphe/learn-git.git master
                    else
                    echo "generated.tf not found"
                    fi
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
