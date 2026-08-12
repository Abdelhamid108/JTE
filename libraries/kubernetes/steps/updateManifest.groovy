// kubernetes/steps/updateManifest.groovy

void call(Map args = [:]) {
    String manifestDir  = config.manifests_dir ?: 'manifests-repo'
    String imageName    = config.image_name
    String version      = args.new_tag ?: env.APP_VERSION
    String targetFolder = args.target_folder

    String tag = "${targetFolder}-${version}"

    dir(manifestDir) {
        sh "find ${targetFolder} -type f \\( -name '*.yaml' -o -name '*.yml' \\) -exec sed -i -E \"s|(image: .*${imageName}):.*|\\\\1:${tag}|g\" {} +"
    }
}