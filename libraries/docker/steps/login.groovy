// steps/login.groovy

void call (){
    String credsId = config.registry_creds ?:
    String registryUrl = config.registry_url ?: 'https://index.docker.io/v1/'
    
    if (!credsId) {
    error "PIPELINE STOPPED: You must provide a 'credentials_id' in your docker pipeline_config.groovy" 
    }

     echo "logging into docker registry..."

     withCredentials([usernamePassword(credentialsId: credsId, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
        sh "echo \$DOCKER_PASS | docker login ${registryUrl} -u \$DOCKER_USER --password-stdin" 
     }

}