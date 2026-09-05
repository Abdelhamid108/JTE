boolean call(Map args = [:]) {
    String repository = args.repository ?: config.image_name
    String tag        = args.tag
    String region     = args.region ?: config.aws_region

    echo "Checking if ECR tag exists: ${repository}:${tag} in ${region}..."

    String output = sh(
        script: """
            aws ecr describe-images \
                --repository-name "${repository}" \
                --image-ids imageTag="${tag}" \
                --region "${region}" 2>&1 || true
        """,
        returnStdout: true
    ).trim()

    if (output.contains("imageTag") || output.contains("imageDigest")) {
        error "Image tag '${tag}' ALREADY EXISTS in ECR repository '${repository}'! You must bump the version tag."
    } else if (output.contains("ImageNotFoundException")) {
        echo "Image tag '${tag}' does not exist in ECR. Safe to build and push."
    } else {
        error "ecr/imageExists: AWS error while checking tag '${tag}':\n${output}"
    }
    return false
}
