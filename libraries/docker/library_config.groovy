// library_config.groovy — Docker library configuration schema

fields {
    required {
        image_name     = String    // Full image name (e.g. 'myregistry/myapp'). Required by build() and tag()
        registry_creds = String    // Jenkins credentials ID for registry auth (usernamePassword). Required by login()
    }
    optional {
        // Build options
        docker_file_name = String    // Dockerfile filename. Default: 'Dockerfile'
        docker_file_dir  = String    // Directory containing the Dockerfile. Default: '.'
        no_cache         = Boolean   // true = build with --no-cache. Default: false

        // Registry
        registry_url     = String    // Docker registry URL. Default: 'https://index.docker.io/v1/'

        // Compose
        compose_file     = String    // Path to compose file. Default: 'docker-compose.yml'
    }
}

steps {
    login
    build
    tag
    push
    promote
    logout
    cleanup
    composeUp
    composeDown
}
