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
        // DO NOT use dir(...) here. Stay in the root directory where '.git' lives.
        withCredentials([usernamePassword(credentialsId: 'git-creds', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            script {
                // Dynamically resolve the working path for the file target
                def workDir = config.workingDir ?: 'src'
                def targetFile = "${workDir}/generated.tf"

                sh """
                    # 1. Configure local Git user signature
                    git config user.name "Jenkins CI"
                    git config user.email "jenkins@yourdomain.com"
                    
                    # 2. Extract current origin URL and inject credentials correctly
                    # This avoids hardcoding your specific github.com repo organization/name
                    CURRENT_URL=\$(git remote get-url origin | sed -E 's|https://||')
                    git remote set-url origin "https://${GIT_USERNAME}:${GIT_PASSWORD}@\${CURRENT_URL}"

                    # 3. Target the file in its actual subfolder location
                    if [ -f "${targetFile}" ]; then
                        echo "Found file at ${targetFile}. Staging..."
                        git add ${targetFile}

                        # Commit safely if differences exist
                        git diff --staged --quiet || git commit -m "Automated update of generated.tf [skip ci]"

                        # Push back to the active tracking branch
                        git push origin HEAD:${BRANCH_NAME}
                    else
                        echo "Warning: ${targetFile} not found! Skipping commit."
                    fi
                """
            }
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
