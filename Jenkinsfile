pipeline {
  agent none
  stages {
    stage('Build') {
      agent { label 'jenkins-jdk11' }
      steps {  mvn("clean install") }
    }
    stage('Deploy') {
      agent { label 'jenkins-jdk11' }
      steps { mvn("deploy -DskipTests=true ") }
    }
  }
}
