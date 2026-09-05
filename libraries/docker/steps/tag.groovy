// docker/steps/tag.groovy

List call(Map args = [:]) {
    String registryURL = args.registry_url ?: config.registry_url
    String imageName   = args.image_name ?: config.image_name
    
    String sourceImage = args.source_image 
    String targetTag   = args.target_tag
    String releaseTag  = args.release_tag ?: env.TAG_NAME 

    if (!sourceImage) error "tagImage: 'source_image' argument is required."
    if (!targetTag)   error "tagImage: 'target_tag' argument is required."
    if (!registryURL || !imageName) error "tagImage: 'registry_url' and 'image_name' are required."

    String targetImage = "${registryURL}/${imageName}:${targetTag}"
    List generatedTags = []
    
    echo "Tagging ${sourceImage} to ${targetImage}"
    sh "docker tag ${sourceImage} ${targetImage}"
    generatedTags.add(targetImage)

    if (releaseTag) {
        String releaseImage = "${registryURL}/${imageName}:${releaseTag}"
        echo "Applying release tag: ${releaseImage}"
        sh "docker tag ${sourceImage} ${releaseImage}"
        generatedTags.add(releaseImage)
    }

    return generatedTags
}