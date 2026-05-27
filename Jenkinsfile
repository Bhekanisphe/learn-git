pipeline {
   agent any
   environment {
      cred = credentials('aws-key')
   }
   stages {
      stage ('Say Hi') {
         steps {
            echo 'Hello world, please run!'
         }
      }
      stage ("Terraform Init"){
         sh 'terraform init'
      }
      stage ("Terraform Plan"){
         sh 'terraform plan'
      }
      stage ("Terraform Apply"){
         sh 'terraform apply'
      }
   }
}
