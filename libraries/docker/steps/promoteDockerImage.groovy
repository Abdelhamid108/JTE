// docker/steps/promoteDockerImage.groovy

void call(Map args = [:]) {
    String sourceImage = args.source_image
    String targetImage = args.target_image


    echo "Promoting Docker image: ${sourceImage} -> ${targetImage}"

    sh """
        docker pull ${sourceImage}
        docker tag ${sourceImage} ${targetImage}
        docker push ${targetImage}
    """
}
