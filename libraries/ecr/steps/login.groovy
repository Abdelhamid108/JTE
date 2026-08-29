// steps/login.groovy — Authenticate Docker against the target ECR registry

void call() {
    String region    = config.aws_region
    String accountId = config.aws_account_id ?: sh(
        script: "aws sts get-caller-identity --query Account --output text",
        returnStdout: true
    ).trim()

    String registry = "${accountId}.dkr.ecr.${region}.amazonaws.com"
    env.ECR_REGISTRY = registry

    echo "ecr/login: authenticating Docker against ${registry}"
    sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${registry}"
}