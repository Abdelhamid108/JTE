boolean call(Map args = [:]) {
    String repository = args.repository ?: config.image_name
    String tag        = args.tag
    String region     = args.region ?: config.aws_region

    echo "Checking if ECR tag exists: ${repository}:${tag} in ${region}..."

    int status = sh(
        script: """
            aws ecr describe-images \
                --repository-name "${repository}" \
                --image-ids imageTag="${tag}" \
                --region "${region}" > /dev/null 2>&1
        """,
        returnStatus: true
    )

    boolean exists = (status == 0)
    if (exists) {
        error "Image tag '${tag}' ALREADY EXISTS in ECR repository '${repository}'! You must bump the version tag."
    } else {
        echo "Image tag '${tag}' does not exist in ECR. Safe to build and push."
    }
    return false
}
