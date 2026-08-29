// steps/promoteImage.groovy — Re-tags verified image in ECR without rebuilding

void call(Map args = [:]) {
    String environment = args.environment
    String baseImage   = "${env.ECR_REGISTRY}/${config.ecr_repository}:${env.APP_VERSION}"
    String envImage    = "${env.ECR_REGISTRY}/${config.ecr_repository}:${environment}-${env.APP_VERSION}"

    echo "ecr/promoteImage: Promoting ${baseImage} -> ${envImage}..."
    sh """
        docker pull ${baseImage}
        docker tag ${baseImage} ${envImage}
        docker push ${envImage}
    """

    echo "ecr/promoteImage: Successfully promoted ${env.APP_VERSION} to '${environment}' in ECR."
}
