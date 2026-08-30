// steps/push.groovy

void call(List tagsToPush) {
    tagsToPush.each { tag ->
        echo "Pushing Docker image: ${tag}"
        retry(3) {
            sh "docker push ${tag}"
        }
    }
}
