// Build script for Codeborne Jenkins
pipeline {
  agent any

  environment {
    APP = "tracker"
    BUILD = "${JOB_NAME.replace('/', '-')}-${BUILD_NUMBER}"
    EMAIL = sh(script: "git show -s --format='%ae' HEAD", returnStdout: true)
    GIT_LAST_CHANGE = sh(script: "git show", returnStdout: true)
    EMAIL_BODY = "Project: ${JOB_NAME}\nBuild Number: ${BUILD_NUMBER}\n\nLast change:\n\n${env.GIT_LAST_CHANGE}"
    COMPOSE = "docker compose -f docker-compose.yml -f docker-compose.codeborne.yml -p ${APP}"
    VERSION = sh(script: 'git describe --tags --always --long', returnStdout: true).trim()
    RUN_TESTS = "docker run --network ${APP}_default -e DB_START= -e DB_URL=jdbc:postgresql://db/${APP} -e BUILD=$BUILD"
  }

  stages {
    stage('Build UI') {
      steps {
        sh "docker build --target build-ui -t ${BUILD}_ui ."
      }
    }
    stage('Test UI') {
      steps {
        sh "docker run -t ${BUILD}_ui npm run test"
      }
    }
    stage('Build Server') {
      steps {
        sh "docker build --target build-server -t ${BUILD}_server ."
      }
    }
    stage('Start test DB') {
      steps {
        sh "$COMPOSE up -d db && sleep 3 && $COMPOSE logs db"
      }
    }
    stage('Test Server') {
      steps {
        sh "$RUN_TESTS --name ${BUILD}_test ${BUILD}_server ./gradlew --no-daemon --info test"
      }
    }
    stage('Build final') {
      when {
        branch 'main'
      }
      steps {
        sh "docker build --build-arg VERSION=$VERSION -t ${APP}-${APP} ."
      }
    }
    stage('Deploy') {
      when {
         branch 'main'
      }
      steps {
        sh "docker logs -t --since 1000h tracker-tracker-1 >> /var/tracker/logs/`date -I`.log"
        sh "$COMPOSE up -d --remove-orphans"
        script {
          def startLogs = sh script: "sleep 9 && $COMPOSE logs $APP", returnStdout: true
          println(startLogs)
          if (!startLogs.contains("Listening on")) error("Failed to start")
          if (EMAIL) mail to: EMAIL, subject: "$APP deployed to test", body: EMAIL_BODY + "\n\nStart logs:\n...\n$startLogs"
        }
      }
    }
  }
  post {
    always {
      sh 'rm -fr build && mkdir -p build/test-results'
      sh 'docker cp ${BUILD}_test:/app/build/test-results build/ || true; docker rm ${BUILD}_test || true'
      sh 'touch build/test-results/**/*.xml || true'
      junit 'build/test-results/**/*.xml'
    }
    failure {
      script {
        if (EMAIL) mail to: EMAIL, subject: "$APP ($BRANCH_NAME) build failed", body: EMAIL_BODY
      }
    }
  }
}
