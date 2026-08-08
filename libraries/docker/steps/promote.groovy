// steps/promote.groovy

void call(Map args = [:]) {
    String sourceImage = args.source_image
    String targetImage = args.target_image

    if (!sourceImage || !targetImage) {
        error "promote: Requires both 'source_image' and 'target_image'."
    }

    sh "docker pull ${sourceImage}"
    sh "docker tag ${sourceImage} ${targetImage}"  
    sh "docker push ${targetImage}"   
} 