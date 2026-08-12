// library_config.groovy — Docker library configuration schema

fields {
    required {
        image_name     = String    // Full image name (e.g. 'myregistry/myapp'). Required by buildImage() and tag()
        registry_creds = String    // Jenkins credentials ID for registry auth (usernamePassword). Required by login()
    }
    optional {
        // Build options
        docker_file_name = String    // Dockerfile filename. Default: 'Dockerfile'
        docker_file_dir  = String    // Directory containing the Dockerfile. Default: '.'
        no_cache         = Boolean   // true = build with --no-cache. Default: false
        install_tools    = Boolean   // true = install agent dependencies (aws-cli, kubectl, etc.). Default: false

        // Registry
        registry_url     = String    // Docker registry URL. Default: 'https://index.docker.io/v1/'

        // Container Validation
        validate_wait_seconds = Integer   // Seconds to wait for container to stabilize. Default: 10
    }
}

steps {
    login
    buildImage
    tag
    push
    promoteDockerImage
    logout
    cleanup
    containerValidate
}
