// steps/composeUp.groovy

void call(Map args = [:]) {
    String composeFile = args.compose_file ?: config.compose_file ?: 'docker-compose.yml'
    Map envVars = args.env_vars ?: [:] 
    
    List envList = envVars.collect { key, value -> "${key}=${value}" }

    echo "Spinning up  environment using ${composeFile}..."
    
    withEnv(envList) {
        sh "docker compose -f ${composeFile} up -d"
    }
}