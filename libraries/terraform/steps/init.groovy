// steps/init.groovy

void call () {  
    // defaults to current directory if not provided
    String targetDir = config.infra_dir ?: '.'
    
    echo "Initializing Terraform "
    dir (targetDir){
        sh "terraform init -reconfigure"
    }
}