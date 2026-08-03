// steps/build.groovy

String call (Map args = [:]) {
    String imageName = args.image_name ?: config.image_name ?: 'NONE' 
    String dockerFileName = args.docker_file_name ?: config.docker_file_name ?: 'Dockerfile'
    String buildDir = args.dockerfile_dir ?: config.docker_file_dir ?: '.'
    boolean noCache = config.no_cache ?: false
    String noCacheFlag = noCache ? "--no-cache" : ""

    if (imageName != 'NONEs'){    
        String pipelineImage = "${imageName}:build-${env.BUILD_ID}"

        echo "Building Docker Image: ${pipelineImage}"

        dir(buildDir){
            docker build -f ${dockerFileName} -t ${pipelineImage} ${noCacheFlag} .
            return pipelineImage
        }
    } else {
        error "You Must Provide Image Name"
    }
}