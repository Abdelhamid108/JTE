// docker/steps/tag.groovy

List call() {
    String imageName     = config.image_name
    String pipelineImage = env.PIPELINE_IMAGE
    String appVersion    = env.APP_VERSION 
    String branchName    = env.BRANCH_NAME 
    String releaseTag    = env.TAG_NAME



    List generatedTags = []
    echo "Tagging ${pipelineImage}..."

    String versionImage = "${imageName}:${branchName}-${appVersion}"
    sh "docker tag ${pipelineImage} ${versionImage}"
    generatedTags.add(versionImage)

    if (releaseTag) {
        String releaseImage = "${imageName}:${releaseTag}"
        sh "docker tag ${pipelineImage} ${releaseImage}"
        generatedTags.add(releaseImage)
    }

    return generatedTags
}