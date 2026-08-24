// steps/init.groovy
//
// AWS auth: none configured here. The agent's ambient IRSA identity
// (via its Kubernetes ServiceAccount) is used automatically by both the
// AWS CLI and Terraform's AWS provider. Remote state (S3/DynamoDB) is
// configured by the Terraform backend block itself, not by JTE.

void call() {
    String targetDir = config.infra_dir ?: '.'

    echo "Initializing Terraform..."
    dir(targetDir) {
        sh "terraform init -reconfigure"
    }
}
