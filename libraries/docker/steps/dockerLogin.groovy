// steps/login.groovy

void call() {
    String credsId     = config.registry_creds
    String registryUrl = config.registry_url ?: 'https://index.docker.io/v1/'

    echo "Logging into Docker registry (${registryUrl})..."

    withCredentials([usernamePassword(credentialsId: credsId, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
        sh "echo \$DOCKER_PASSWORD | docker login ${registryUrl} -u \$DOCKER_USERNAME --password-stdin"
    }
}