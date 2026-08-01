// steps/validate.groovy

def call (Map config = [:]) {
    // defaults to current directory if not provided
    def targetDir = config.dir ?: '.'

    println "Validating terraform configuration syntax..."
    dir(targetDir){ 
        sh "terraform fmt -check || echo 'Warning: Terraform code layout is unformatted.' "
        sh "terraform validate"
    }
}