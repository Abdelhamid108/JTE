// kubernetes/steps/updateManifest.groovy

void call(Map args = [:]) {
    String manifestDir = args.manifest_dir ?: config.manifests_dir ?: 'manifests-repo'
    String imageName   = args.image_name   ?: config.image_name  
    String newTag      = args.new_tag      ?: env.APP_VERSION      ?: readVersion()

    if (!imageName || !newTag) {
        error "updateManifest: Both 'image_name' and 'new_tag' are required. (Received image_name='${imageName}', new_tag='${newTag}')"
    }

    echo "Updating image tag in manifests: ${imageName}:${newTag}"

    dir(manifestDir) {
        sh "find . -type f \\( -name '*.yaml' -o -name '*.yml' \\) -exec sed -i -E \"s|(image: .*${imageName}):.*|\\\\1:${newTag}|g\" {} +"
    }

    echo "Manifests updated successfully."
}