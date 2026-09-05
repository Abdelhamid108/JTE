// steps/push.groovy — Push Docker image to registry

void call(def images) {
    if (!images) {
        error "docker/push: No image(s) provided to push."
    }

    List imageList = (images instanceof List) ? images : [images]

    imageList.each { image ->
        echo "docker/push: Pushing Docker image: ${image}"
        retry(3) {
            sh "docker push ${image}"
        }
    }
}



