// docker/steps/promoteDockerImage.groovy

void call(Map args = [:]) {
    String imageName   = args.image_name   ?: config.image_name ?: pipelineConfig?.libraries?.docker?.image_name ?: pipelineConfig?.libraries?.kubernetes?.image_name
    String version     = args.version      ?: env.APP_VERSION   ?: readVersion()
    String sourceImage = args.source_image ?: (imageName ? "${imageName}:dev-${version}" : null)
    String targetImage = args.target_image ?: (imageName ? "${imageName}:prod-${version}" : null)

    if (!sourceImage || !targetImage) {
        error "promoteDockerImage: Requires 'source_image' and 'target_image' (or configured 'image_name')."
    }

    echo "Promoting Docker image: ${sourceImage} -> ${targetImage}"

    sh """
        if ! docker pull ${sourceImage}; then
            echo "Primary tag ${sourceImage} not found, trying fallback ${imageName}:${version}..."
            docker pull ${imageName}:${version}
            docker tag ${imageName}:${version} ${sourceImage}
        fi
        docker tag ${sourceImage} ${targetImage}
        docker push ${targetImage}
    """
}
