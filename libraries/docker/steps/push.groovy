// steps/push.groovy — Push Docker image to registry

void call(def tagsToPush = null) {
    String registryURL = config.registry_url ?: pipelineConfig.libraries?.docker?.registry_url
    String imageName   = config.image_name   ?: pipelineConfig.libraries?.docker?.image_name

    List list = []
    if (tagsToPush == null) {
        if (env.PIPELINE_IMAGE) {
            list = [env.PIPELINE_IMAGE]
        }
    } else if (tagsToPush instanceof String) {
        if (tagsToPush.contains('/')) {
            list = [tagsToPush]
        } else {
            list = ["${registryURL}/${imageName}:${tagsToPush}"]
        }
    } else if (tagsToPush instanceof List) {
        list = tagsToPush
    }

    list.each { tag ->
        echo "docker/push: Pushing Docker image: ${tag}"
        retry(3) {
            sh "docker push ${tag}"
        }
    }
}



