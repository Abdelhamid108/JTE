void call (list tagsToPush){
    if (!tagsToPush || tagsToPush.isEmpty()) {
        error "No Tags provided to push "
    }
    tagsToPush.each { tag ->
        echo "Pushing image: ${tag}"
        sh "docker push ${tag}"
    }
}
