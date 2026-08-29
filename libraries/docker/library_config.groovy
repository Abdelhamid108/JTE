// library_config.groovy — Generic Docker library configuration schema

fields {
    optional {
        image_name            = String
        registry_url          = String
        registry_creds        = String
        docker_file_name      = String
        docker_file_dir       = String
        container_port        = Integer
        health_check_path     = String
        validate_wait_seconds = Integer
    }
}

steps {
    login
    logout
    buildImage
    tag
    push
    promoteDockerImage
    containerValidate
    cleanup
}
