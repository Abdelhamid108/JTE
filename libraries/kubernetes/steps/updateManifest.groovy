// kubernetes/steps/updateManifest.groovy

void call(Map args = [:]) {
    String manifestDir  = args.manifest_dir  ?: config.manifests_dir ?: 'manifests-repo'
    String imageName    = args.image_name    ?: config.image_name    ?: pipelineConfig?.libraries?.kubernetes?.image_name ?: pipelineConfig?.libraries?.docker?.image_name
    String version      = args.new_tag       ?: env.APP_VERSION      ?: readVersion()
    String targetFolder = args.target_folder ?: error("updateManifest: 'target_folder' is required.")

    if (!imageName || !version) {
        error "updateManifest: Both 'image_name' and 'new_tag' are required."
    }

    String tag = version.startsWith("${targetFolder}-") ? version : "${targetFolder}-${version}"

    echo "Updating image tag in manifests (${targetFolder}/): ${imageName}:${tag}"

    dir(manifestDir) {
        sh "find ${targetFolder} -type f \\( -name '*.yaml' -o -name '*.yml' \\) -exec sed -i -E \"s|(image: .*${imageName}):.*|\\\\1:${tag}|g\" {} +"
    }

    echo "Manifests updated successfully for ${targetFolder} with tag ${tag}."
}