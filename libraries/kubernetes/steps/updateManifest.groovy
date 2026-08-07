// kubernetes/steps/updateManifest.groovy

void call(Map args = [:]) {
    String manifestDir = args.manifest_dir ?: config.manifests_dir ?: 'manifests-repo'
    String imageName   = args.image_name   ?: config.image_name
    String newTag      = args.new_tag

    if (!imageName || !newTag) {
        error "updateManifest: 'image_name' and 'new_tag' are required."
    }

    echo "Updating image tag in manifests: ${imageName}:${newTag}"

    dir(manifestDir) {
        sh "find . -type f -name '*.yaml' -exec sed -i -E \"s|(image: .*${imageName}):.*|\\\\1:${newTag}|g\" {} +"
    }

    echo "Manifests updated successfully."
}