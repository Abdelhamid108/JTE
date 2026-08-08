// steps/login.groovy

void call(Map args = [:]) {
    String credsId     = args.registry_creds ?: config.registry_creds
    String registryUrl = args.registry_url   ?: config.registry_url ?: 'https://index.docker.io/v1/'

    if (!credsId) {
        error "login: You must provide a 'registry_creds' credential ID."
    }

    echo "Logging into Docker registry (${registryUrl})..."

    withCredentials([usernamePassword(credentialsId: credsId, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
        sh "echo \$DOCKER_PASSWORD | docker login ${registryUrl} -u \$DOCKER_USERNAME --password-stdin"
    }
}