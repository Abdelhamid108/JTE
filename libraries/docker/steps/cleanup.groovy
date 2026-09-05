// steps/cleanup.groovy

void call(List imagesToClean = []) {
    echo "Cleaning up local Docker images to save disk space..."
    
    if (imagesToClean) {
        imagesToClean.each { img ->
            sh "docker rmi ${img} || true"
        }
    } else if (env.IMAGE_URI) {
        sh "docker rmi ${env.IMAGE_URI} || true"
    }
}