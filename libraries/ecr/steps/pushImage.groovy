// steps/pushImage.groovy — Push base image and environment tag to ECR

void call(Map args = [:]) {
    String environment = args.environment
    if (!environment) { error "ecr/pushImage: 'environment' is required." }

    String baseImage = "${env.ECR_REGISTRY}/${config.ecr_repository}:${env.APP_VERSION}"
    String envImage  = "${env.ECR_REGISTRY}/${config.ecr_repository}:${environment}-${env.APP_VERSION}"

    echo "ecr/pushImage: Pushing ${baseImage} and ${envImage}..."
    sh """
        docker push ${baseImage}
        docker tag  ${baseImage} ${envImage}
        docker push ${envImage}
    """
    echo "ecr/pushImage: Done."
}
