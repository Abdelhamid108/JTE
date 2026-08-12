// docker/steps/containerValidate.groovy

void call() {
    String image         = env.PIPELINE_IMAGE
    String containerName = "validate-${env.BUILD_ID}"
    int waitSeconds      = config.validate_wait_seconds ?: 10



    echo "  CONTAINER VALIDATION — ${image}"

    try {
        sh "docker run -d --name ${containerName} ${image}"

        echo "Waiting ${waitSeconds}s for container to stabilize..."
        sleep(waitSeconds)

        String status = sh(
            script: "docker inspect -f '{{.State.Status}}' ${containerName}",
            returnStdout: true
        ).trim()

        if (status != 'running') {
            String logs = sh(script: "docker logs ${containerName} 2>&1 || true", returnStdout: true).trim()
            error "containerValidate: Container exited with status '${status}'.\n--- Container Logs ---\n${logs}"
        }

        echo "Container is running (status: ${status})"


        echo "  CONTAINER VALIDATION PASSED"

    } finally {
        sh "docker stop ${containerName} 2>/dev/null || true"
        sh "docker rm -f ${containerName} 2>/dev/null || true"
    }
}
