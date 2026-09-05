// steps/ecrHooks.groovy — JTE Lifecycle Hooks for AWS ECR & Push Governance

@Validate
void validateConfig() {
    if (!config.aws_region) {
        error "ecr: 'aws_region' is required in pipeline_config.groovy."
    }
    echo "ecr [@Validate]: config OK — region='${config.aws_region}'"
}

@BeforeStep
void onBeforeStep() {
    String currentStep = hookContext?.step

    if (currentStep == 'push') {
        echo "ecr [@BeforeStep 'push']: Auto-authenticating Docker with AWS ECR via assumeRole + login..."
        assumeRole {
            login()
        }
    }

    if (currentStep == 'retagImage') {
        String repo     = config.image_name 
        String registry = config.ecr_registry ?: ''
        String srcTag   = (env.TAG_NAME && env.TAG_NAME.contains('-rc')) ? "dev-${env.GIT_SHORT_SHA}" : (env.TAG_NAME ? "test-${env.TAG_NAME}-rc" : '')

        if (srcTag && repo) {
            String fullSourceImage = registry ? "${registry}/${repo}:${srcTag}" : "${repo}:${srcTag}"
            echo "ecr [@BeforeStep 'retagImage']: Scanning source image ${fullSourceImage} with Trivy before promotion..."
            assumeRole {
                scanImage(image_uri: fullSourceImage, fresh_pull: true)
            }
        }
    }
}
