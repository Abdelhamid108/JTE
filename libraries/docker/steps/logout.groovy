// steps/logout.groovy

void call() {
    String registryUrl = config.registry_url ?: 'https://index.docker.io/v1/'

    echo "Logging out of Docker Registry to clear credentials..."
    sh "docker logout ${registryUrl} || true"
}