// steps/buildImage.groovy — Build the application container image

String call(Map args = [:]) {
    String dockerFile = args.dockerfile ?: config.dockerfile_path ?: config.docker_file_name ?: 'Dockerfile'
    String context    = args.build_context ?: config.build_context ?: config.docker_file_dir ?: '.'

    String registryURL = args.registry_url ?: config.registry_url
    String imageName   = args.image_name   ?: config.image_name
    String tag         = args.tag ?: 'latest'
    String fullImage   = "${registryURL}/${imageName}:${tag}"

    echo "docker/buildImage: Building ${fullImage} (Dockerfile: ${dockerFile}, context: ${context})"

    if (context != '.') {
        sh "docker build --pull -f ${dockerFile} -t ${fullImage} ${context}"
    } else {
        sh "docker build --pull -f ${dockerFile} -t ${fullImage} ."
    }

    env.IMAGE_URI = fullImage
    return fullImage
}