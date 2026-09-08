// steps/outputArtifacts.groovy — Export and archive Terraform outputs
void call() {
    String targetDir = config.infra_dir ?: '.'
    dir(targetDir) {
        echo "Exporting and archiving Terraform outputs..."
        sh "terraform output -json > terraform-output.json || true"
        sh "terraform output -no-color > terraform-output.txt || true"
        archiveArtifacts artifacts: "terraform-output.json, terraform-output.txt", allowEmptyArchive: true
    }
}
