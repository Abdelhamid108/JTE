// steps/validate.groovy

void call () {
    // defaults to current directory if not provided
    String targetDir = config.infra_dir ?: '.'

    echo "Validating terraform configuration syntax..."
    dir(targetDir){ 
        sh "terraform fmt -check || echo 'Warning: Terraform code layout is unformatted.' "
        sh "terraform validate"
    }
}