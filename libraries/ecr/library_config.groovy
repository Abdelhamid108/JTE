// library_config.groovy — ECR library configuration schema
//
// Builds and pushes the PetClinic Docker image to AWS ECR. Authentication
// relies exclusively on the ambient IAM identity available to the Jenkins
// agent (IRSA — IAM Roles for Service Accounts). No static AWS access keys
// are configured or accepted by this library.

fields {
    required {
        aws_region     = String
        ecr_repository = String   // e.g. 'petclinic' (repo name, not full URI)
    }
    optional {
        image_name       = String   // Default: same as ecr_repository
        dockerfile_path  = String   // Default: 'application/Dockerfile'
        build_context    = String   // Default: 'application'
        aws_account_id   = String   // Optional — resolved via 'aws sts get-caller-identity' if not set
    }
}

steps {
    login
    buildImage
    pushImage
}
