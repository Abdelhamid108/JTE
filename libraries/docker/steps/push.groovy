// steps/push.groovy

void call(List tagsToPush) {

    tagsToPush.each { tag ->
        echo "Pushing Docker image: ${tag}"
        sh "docker push ${tag}"
    }
}
