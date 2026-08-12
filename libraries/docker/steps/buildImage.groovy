// steps/buildImage.groovy

String call() {
    String imageName      = config.image_name
    String dockerFileName = config.docker_file_name ?: 'Dockerfile'
    String buildDir       = config.docker_file_dir ?: '.'



    String pipelineImage = "${imageName}:build-${env.BUILD_ID}"

    echo "Building Docker Image: ${pipelineImage}"

    dir(buildDir) {
        sh "docker build -f ${dockerFileName} -t ${pipelineImage} ."
    }

    env.PIPELINE_IMAGE = pipelineImage
    return pipelineImage
}