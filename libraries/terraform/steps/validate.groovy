// steps/validate.groovy
//
// Policy: formatting is a WARNING only (non-blocking) — 'terraform fmt
// -check' failures do not stop the pipeline. 'terraform validate' failures
// DO stop the pipeline, since they indicate genuinely broken configuration
// rather than a style nit.

void call() {
    String targetDir = config.infra_dir ?: '.'

    echo "Validating terraform configuration syntax..."
    dir(targetDir) {
        sh "terraform fmt -check || echo 'Warning: Terraform code layout is unformatted (non-blocking).'"
        sh "terraform validate"
    }
}
