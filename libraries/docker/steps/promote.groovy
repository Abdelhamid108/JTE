void (Map args = [:]){
    String sourceImage = args.source_image
    String targetImage = args.target_image

    if (! sourceImage || !targetImage) error "Pipeline Failed: Promote requires both 'source image' and 'target image'"

    sh "docker pull ${sourceImage}"
    sh "docker tag ${sourceImage} ${targetImage}"  
    sh "docker push ${$targetImage}"   
} 