// kubernetes/steps/updateManifest.groovy

void call(Map args = [:]) {
    String manifestDir  = args.manifest_dir  ?: config.manifests_dir ?: 'manifests-repo'
    String imageName    = args.image_name    ?: config.image_name    ?: pipelineConfig?.libraries?.kubernetes?.image_name ?: pipelineConfig?.libraries?.docker?.image_name
    String newTag       = args.new_tag       ?: env.APP_VERSION      ?: readVersion()
    
    String targetFolder = args.target_folder ?: error("updateManifest: 'target_folder' is required to prevent cross-environment contamination.")

    if (!imageName || !newTag) {
        error "updateManifest: Both 'image_name' and 'new_tag' are required. (Received image_name='${imageName}', new_tag='${newTag}')"
    }

    echo "Updating image tag in manifests (${targetFolder}/): ${imageName}:${newTag}"

    dir(manifestDir) {
        sh "find ${targetFolder} -type f \\( -name '*.yaml' -o -name '*.yml' \\) -exec sed -i -E \"s|(image: .*${imageName}):.*|\\\\1:${newTag}|g\" {} +"
    }

    echo "Manifests updated successfully for ${targetFolder}."
}