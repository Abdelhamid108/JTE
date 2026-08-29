// steps/buildImage.groovy — Build the application Docker image

void call(Map args = [:]) {
    String dockerfile = config.dockerfile_path ?: 'Dockerfile'
    String context    = config.build_context   ?: '.'
    String imageUri   = "${env.ECR_REGISTRY}/${config.ecr_repository}:${env.APP_VERSION}"

    echo "ecr/buildImage: Building ${imageUri} from ${dockerfile}"
    sh "docker build -f ${dockerfile} -t ${imageUri} ${context}"

    env.IMAGE_URI = imageUri
}
