// steps/dockerHooks.groovy — JTE Lifecycle Hooks for Docker Governance


@AfterStep
void onAfterStep() {
    String currentStep = hookContext?.step

    if (currentStep == 'buildImage') {
        echo "docker [@AfterStep 'buildImage']: Enforcing Container Smoke Validation & Image Security Scan..."
        containerValidate()
    }
}

@CleanUp
void onCleanUp() {
    try {
        if (getContext(hudson.FilePath.class) != null) {
            cleanWs()
        }
    } catch (Exception e) {
        // Node workspace already released
    }
}
