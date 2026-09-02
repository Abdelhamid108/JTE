// steps/buildImage.groovy — Build the application container image

String call(Map args = [:]) {
    String dockerFile = args.dockerfile ?: config.dockerfile_path ?: config.docker_file_name ?: 'Dockerfile'
    String context    = args.build_context ?: config.build_context ?: config.docker_file_dir ?: '.'

    String registryURL = args.registry_url ?: config.registry_url
    String imageName   = args.image_name   ?: config.image_name
    String tag         = args.tag ?: (env.GIT_TAG ? "dev-${env.GIT_TAG}" : (env.BUILD_TAG ? "dev-${env.BUILD_TAG}" : (env.APP_VERSION ?: 'latest')))
    String fullImage   = "${registryURL}/${imageName}:${tag}"

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