// steps/buildImage.groovy — Build the application container image

String call(Map args = [:]) {
    String dockerFile = args.dockerfile ?: config.dockerfile_path ?: config.docker_file_name ?: 'Dockerfile'
    String context    = args.build_context ?: config.build_context ?: config.docker_file_dir ?: '.'

    String ecrRepo    = pipelineConfig.libraries?.ecr?.ecr_repository
    String imageName  = args.image_name ?: config.image_name ?: (env.ECR_REGISTRY && ecrRepo ? "${env.ECR_REGISTRY}/${ecrRepo}" : (ecrRepo ?: "app"))
    String tag        = args.tag ?: config.image_tag ?: env.APP_VERSION ?: "build-${env.BUILD_ID}"
    String fullImage  = "${imageName}:${tag}"

    echo "docker/buildImage: Building ${fullImage} (Dockerfile: ${dockerFile}, context: ${context})"

    if (context != '.') {
        sh "docker build -f ${dockerFile} -t ${fullImage} ${context}"
    } else {
        sh "docker build -f ${dockerFile} -t ${fullImage} ."
    }

    env.IMAGE_URI      = fullImage
    env.PIPELINE_IMAGE = fullImage
    return fullImage
}