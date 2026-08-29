// library_config.groovy — ECR library configuration schema

fields {
    required {
        aws_region     = String
        ecr_repository = String
    }
    optional {
        aws_account_id = String
    }
}

steps {
    login
    pushImage
    promoteImage
}
