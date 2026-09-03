// steps/dockerHooks.groovy — JTE Lifecycle Hooks for Docker Governance

@Validate
void validateConfig() {
    if (!config.registry_url) {
        error "docker: 'registry_url' is required in pipeline_config.groovy."
    }
    if (!config.image_name) {
        error "docker: 'image_name' is required in pipeline_config.groovy."
    }
    echo "docker [@Validate]: config OK — registry='${config.registry_url}', image='${config.image_name}'"
}

@AfterStep
void onAfterStep() {
    String currentStep = hookContext?.step

    if (currentStep == 'buildImage') {
        echo "docker [@AfterStep 'buildImage']: Enforcing Container Smoke Validation & Image Security Scan..."
        containerValidate()
        scanImage()
    }
}

@CleanUp
void onCleanUp() {
    echo "docker [@CleanUp]: Purging build workspace and cleaning up..."
    try {
        cleanWs()
    } catch (Exception e) {
        echo "docker [@CleanUp]: Warning during cleanWs: ${e.message}"
    }
}
