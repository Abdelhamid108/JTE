// docker/steps/tag.groovy

List call(Map args = [:]) {
    String imageName = args.image_name ?: config.image_name 
    String pipelineImage = args.pipeline_image ?: env.PIPELINE_IMAGE
    
    if (!pipelineImage || !imageName) {
        error "PIPELINE STOPPED: Missing image name or pipeline image for docker.tag()"
    }

    String gitCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    String branchName = env.BRANCH_NAME ?: 'dev'
    String releaseTag = env.TAG_NAME

    String shaImage = "${imageName}:${gitCommit}"
    String cacheImage = "${imageName}:${branchName}" 

    echo "Tagging ${pipelineImage}..."
    sh "docker tag ${pipelineImage} ${shaImage}"
    sh "docker tag ${pipelineImage} ${cacheImage}"
     
    List generatedTags = [shaImage, cacheImage]

    if (releaseTag) {

        String releaseImage = "${imageName}:${releaseTag}" 
        sh "docker tag ${pipelineImage} ${releaseImage}"
        generatedTags.add(releaseImage)
    } 
     
    return generatedTags
}