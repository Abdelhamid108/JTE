// steps/init.groovy

void call() {  
    String targetDir = config.infra_dir ?: '.'
    
    echo "Initializing Terraform..."
    dir(targetDir) {
        sh "terraform init -reconfigure"
    }
}