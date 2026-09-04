// library_config.groovy — ECR library configuration schema
// ECR is an auth adapter only. All image operations are handled by the docker library.

fields {
    required {
        aws_region   = String   // e.g. "us-east-1"
        ecr_registry = String   // e.g. "069089526123.dkr.ecr.us-east-1.amazonaws.com"
    }
}

steps {
    ecrHooks
    login
    imageExists
    retagImage
}
