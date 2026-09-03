// steps/login.groovy — AWS ECR credential handshake (auth adapter only)
// Sets env.ECR_REGISTRY so downstream docker steps can reference the full registry URL.

void call() {
    String registry = config.ecr_registry
    String region   = config.aws_region

    echo "ecr/login: Authenticating Docker against ${registry}..."
    retry(3) {
        sh "set -o pipefail; aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${registry}"
    }

    env.ECR_REGISTRY = registry
    echo "ecr/login: Done. ECR_REGISTRY=${registry}"
}
