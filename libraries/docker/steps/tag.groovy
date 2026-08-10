// docker/steps/tag.groovy

List call(Map args = [:]) {
    String imageName     = args.image_name    ?: config.image_name
    String pipelineImage = args.pipeline_image ?: env.PIPELINE_IMAGE ?: (imageName ? "${imageName}:build-${env.BUILD_ID}" : null)
    String appVersion    = args.app_version   ?: env.APP_VERSION ?: readVersion()

    if (!pipelineImage || !imageName) {
        error "PIPELINE STOPPED: Missing image name or pipeline image for docker.tag()"
    }

    String branchName = env.BRANCH_NAME ?: 'dev'
    String releaseTag = env.TAG_NAME

    List generatedTags = []

    echo "Tagging ${pipelineImage}..."

    // Primary tag: application version or git commit SHA as fallback
    if (appVersion) {
        String versionImage = "${imageName}:${branchName}-${appVersion}"
        sh "docker tag ${pipelineImage} ${versionImage}"
        generatedTags.add(versionImage)
    } else {
        String gitCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        String shaImage = "${imageName}:${branchName}-${gitCommit}"
        sh "docker tag ${pipelineImage} ${shaImage}"
        generatedTags.add(shaImage)
    }

    // Cache tag
    String cacheImage = "${imageName}:${branchName}"
    sh "docker tag ${pipelineImage} ${cacheImage}"
    generatedTags.add(cacheImage)

    if (releaseTag) {
        String releaseImage = "${imageName}:${releaseTag}"
        sh "docker tag ${pipelineImage} ${releaseImage}"
        generatedTags.add(releaseImage)
    }

    return generatedTags
}