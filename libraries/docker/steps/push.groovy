// steps/push.groovy — Push Docker image to registry

void call(def images = null) {
    def targets = images ?: env.IMAGE_URI
    if (!targets) {
        error "docker/push: No image(s) provided to push, and env.IMAGE_URI is not set."
    }

    List imageList = (targets instanceof List) ? targets : [targets]
    String registry = config.registry_url ?: ''
    String repo     = config.image_name   ?: ''

    imageList.each { item ->
        String fullImage = item
        if (repo && !item.contains('/')) {
            String tag = item.startsWith(':') ? item.substring(1) : item
            fullImage = registry ? "${registry}/${repo}:${tag}" : "${repo}:${tag}"
        }

        echo "docker/push: Pushing Docker image: ${fullImage}"
        retry(3) {
            sh "docker push ${fullImage}"
        }
    }
}



