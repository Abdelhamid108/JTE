// library_config.groovy — ECR library configuration schema

fields {
    required {
        aws_region     = String
        ecr_repository = String
    }
    optional {
        aws_account_id  = String
        dockerfile_path = String   // Path to Dockerfile. Default: 'Dockerfile'
        build_context   = String   // Docker build context directory. Default: '.'
    }
}

steps {
    login
    buildImage
    pushImage
    promoteImage
}
