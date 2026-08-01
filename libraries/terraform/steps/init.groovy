// steps/init.groovy

def call (Map config = [:]) {  
    // defaults to current directory if not provided
    def targetDir = config.dir ?: '.'

    println "Initializing Terraform "
    dir (targetDir){
        sh "terraform init -reconfigure"
    }
}