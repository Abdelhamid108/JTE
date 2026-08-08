// docker/steps/containerValidate.groovy

void call(Map args = [:]) {
    String defaultImage   = config.image_name ? "${config.image_name}:build-${env.BUILD_ID}" : null
    String image          = args.pipeline_image ?: env.PIPELINE_IMAGE ?: defaultImage
    String containerName = args.container_name ?: "validate-${env.BUILD_ID}"
    int waitSeconds      = args.wait_seconds   ?: config.validate_wait_seconds ?: 10
    String healthUrl     = args.health_url     ?: config.health_url

    if (!image) {
        error "containerValidate: 'pipeline_image' is required."
    }

    echo "═══════════════════════════════════════════"
    echo "  CONTAINER VALIDATION — ${image}"
    echo "═══════════════════════════════════════════"

    try {
        // Start container in detached mode
        sh "docker run -d --name ${containerName} ${image}"

        // Give the application time to start
        echo "Waiting ${waitSeconds}s for container to stabilize..."
        sleep(waitSeconds)

        // Check the container is still running (didn't crash on startup)
        String status = sh(
            script: "docker inspect -f '{{.State.Status}}' ${containerName}",
            returnStdout: true
        ).trim()

        if (status != 'running') {
            // Grab logs before failing for diagnostics
            String logs = sh(script: "docker logs ${containerName} 2>&1 || true", returnStdout: true).trim()
            error "containerValidate: Container exited with status '${status}'.\n--- Container Logs ---\n${logs}"
        }

        echo "Container is running (status: ${status})"

        // Optional: hit a health endpoint inside the container
        if (healthUrl) {
            echo "Checking health endpoint: ${healthUrl}"
            int rc = sh(script: "docker exec ${containerName} curl -sf ${healthUrl}", returnStatus: true)
            if (rc != 0) {
                String logs = sh(script: "docker logs ${containerName} 2>&1 || true", returnStdout: true).trim()
                error "containerValidate: Health check failed (exit code ${rc}).\n--- Container Logs ---\n${logs}"
            }
            echo "Health check passed."
        }

        echo "  CONTAINER VALIDATION PASSED"
        echo "═══════════════════════════════════════════"

    } finally {
        // Always cleanup the validation container
        sh "docker stop ${containerName} 2>/dev/null || true"
        sh "docker rm -f ${containerName} 2>/dev/null || true"
    }
}
