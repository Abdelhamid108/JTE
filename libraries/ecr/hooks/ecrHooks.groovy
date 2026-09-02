// hooks/ecrHooks.groovy — JTE Lifecycle Hooks for AWS ECR & Push Governance

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

    if (currentStep in ['imageExists', 'retagImage', 'push']) {
        echo "ecr [@BeforeStep '${currentStep}']: Auto-authenticating with AWS ECR via assumeRole + login..."
        assumeRole {
            login()
        }
    }

    if (currentStep == 'push') {
        echo "ecr [@BeforeStep 'push']: Enforcing prerequisite Container Smoke Validation & CVE Scan..."
        containerValidate()
        scanImage()
    }
}
