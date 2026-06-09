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
        // Stay in the root directory where '.git' lives
        withCredentials([usernamePassword(credentialsId: 'git-creds', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh '''
                # 1. Clear the security warning by using single quotes for the script block 
                # and letting the shell handle the environment variables.
                git config user.name "Jenkins CI"
                git config user.email "jenkins@yourdomain.com"
                
                # 2. Fix and update remote URL dynamically
                CURRENT_URL=$(git remote get-url origin | sed -E 's|https://||')
                git remote set-url origin "https://${GIT_USERNAME}:${GIT_PASSWORD}@${CURRENT_URL}"

                # 3. Use find to search for generated.tf anywhere in the workspace
                TARGET_FILE=$(find . -name "generated.tf" -not -path '*/.*' | head -n 1)

                if [ -n "$TARGET_FILE" ]; then
                    echo "Success: Found generated file at: $TARGET_FILE"
                    
                    # Stage the exact path found
                    git add "$TARGET_FILE"

                    # Commit only if there are actual diffs
                    git diff --staged --quiet || git commit -m "Automated update of generated.tf [skip ci]"

                    # Push back up to the active branch
                    git push origin HEAD:${BRANCH_NAME}
                else
                    echo "ERROR: generated.tf could not be found anywhere in the workspace!"
                    echo "--- Current workspace files ---"
                    find . -maxdepth 3 -not -path '*/.*'
                    exit 1
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
