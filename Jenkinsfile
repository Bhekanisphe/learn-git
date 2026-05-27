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
         steps{
         sh 'terraform init'
         }
      }
      stage ("Terraform Plan"){
         steps{
         sh 'terraform plan'
         }
      }
      stage ("Terraform Apply"){
         steps{
         sh 'terraform apply'
         }
      }
   }
}
