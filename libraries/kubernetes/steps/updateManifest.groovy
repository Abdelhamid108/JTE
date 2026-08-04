// steps/updateManifest.groovy

void call (Map args = [:]){
    String manifestDir = args.manifest_dir ?: config.manifest_dir ?: 'k8s'
    String branchName = env.BRANCH_NAME
    String imageName   = args.image_name   ?: config.image_name
    String newTag      = args.new_tag     

    if(!imageName || ! newTag){
        error "updating manifests requires 'image name' and 'new tag'"
    }
 

    dir(manifest_dir){
        imagesToUpdate.each { image_name, newtag }
       sh  "find . -type f -name '*.yaml' -exec sed -i -E "s|(image: .*${imageName}):.*|\\1:${newTag}|g" {} +"
    }

}