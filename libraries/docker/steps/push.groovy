// steps/push.groovy

void call(List tagsToPush) {
    if (!tagsToPush || tagsToPush.isEmpty()) {
        error "push: No tags provided to push."
    }
    tagsToPush.each { tag ->
        echo "Pushing Docker image: ${tag}"
        sh "docker push ${tag}"
    }
}
