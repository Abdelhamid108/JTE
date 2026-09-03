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

    if (currentStep == 'retagImage') {
        echo "ecr [@BeforeStep 'retagImage']: Scanning source image before promotion..."
        Map args = hookContext.args ? hookContext.args[0] : [:]
        String imageUri = "${config.ecr_registry}/${pipelineConfig.libraries.docker.image_name}:${args.source_tag}"

        assumeRole {
            scanImage(image_uri: imageUri, fresh_pull: true)
        }
    }
}



