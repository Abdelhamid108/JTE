void call (Map args = [:]) {
    String registryUrl= args.registry_url ?: config.registry_url ?: 'https://index.docker.io/v1/'

    echo "Logging out of Docker Registry to clear credentials..."
    sh "docker logout ${registryUrl} || true "

}