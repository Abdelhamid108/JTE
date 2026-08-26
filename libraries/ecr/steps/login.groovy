// steps/login.groovy — Authenticate Docker against the target ECR registry.
//
// Contract:
//   input : config.aws_region, config.aws_account_id (optional)
//   output: env.ECR_REGISTRY, docker CLI authenticated for subsequent push
//   fails : if the agent's IRSA identity lacks ecr:GetAuthorizationToken
//
// No static AWS credentials are used here — the Jenkins agent pod is
// expected to run under a Kubernetes ServiceAccount bound to an IAM role
// via IRSA, so the AWS CLI resolves temporary credentials automatically.

void call(Map args = [:]) {

    String region = args.aws_region ?: config.aws_region
    String accountId = args.aws_account_id ?: config.aws_account_id
    String credentialsId = args.aws_credentials_id ?: config.aws_credentials_id

    if (!region) {
        error "ecr/login: 'aws_region' is required."
    }

    if (!credentialsId) {
        error "ecr/login: 'aws_credentials_id' is required."
    }

    withCredentials([
        usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'AWS_ACCESS_KEY_ID',
            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
        )
    ]) {

        if (!accountId) {
            accountId = sh(
                script: "aws sts get-caller-identity --query Account --output text",
                returnStdout: true
            ).trim()
        }

        String registry =
            "${accountId}.dkr.ecr.${region}.amazonaws.com"

        env.ECR_REGISTRY = registry

        echo "ecr/login: authenticating Docker against ${registry}"

        sh """
            aws ecr get-login-password --region ${region} |
            docker login \
              --username AWS \
              --password-stdin ${registry}
        """
    }
}