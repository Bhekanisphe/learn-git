pipeline {
   agent any
   stages {
       stage('Notify GitLab') {
           steps {
               updateGitlabCommitStatus name: 'build', state: 'pending'
               // build steps here
               updateGitlabCommitStatus name: 'build', state: 'success'
           }
          
       }
      stage ('Say Hi') {
         echo 'Hello world'
      }
   }
}
