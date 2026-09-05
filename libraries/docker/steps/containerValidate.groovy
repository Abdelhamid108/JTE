// docker/steps/containerValidate.groovy — Validates container health with dynamic port allocation

void call(Map args = [:]) {
    String containerName = "validate-${env.BUILD_ID}"
    int containerPort    = (args.container_port ?: config.container_port) as Integer
    String healthPath    = args.health_check_path ?: config.health_check_path 
    String image         = args.image ?: env.IMAGE_URI 
    int waitSeconds      = args.wait_seconds ?: (config.validate_wait_seconds ?: 150)
    int maxRetries       = (args.max_retries ?: (waitSeconds / 5)) as Integer
    int retryInterval    = 5

    if (!image) {
        error "docker/containerValidate: No image specified and env.IMAGE_URI is not set."
    }

    echo "\n  CONTAINER SMOKE TEST — ${image}"

    try {
        sh "docker run -d --name ${containerName} -p 0:${containerPort} ${image}"

        String hostPort = sh(
            script: "docker inspect --format='{{(index (index .NetworkSettings.Ports \"${containerPort}/tcp\") 0).HostPort}}' ${containerName}",
            returnStdout: true
        ).trim()

        if (!hostPort) {
            String logs = sh(script: "docker logs ${containerName} 2>&1 || true", returnStdout: true).trim()
            error "docker/containerValidate: Container failed to expose port ${containerPort}.\n--- Logs ---\n${logs}"
        }

        String healthUrl = "http://localhost:${hostPort}${healthPath}"
        echo "  Health Endpoint: ${healthUrl} (host port ${hostPort} -> container port ${containerPort})"

        boolean healthy = false
        for (int i = 1; i <= maxRetries; i++) {
            echo "containerValidate: Attempt ${i}/${maxRetries}..."
            if (sh(script: "curl -s -f -o /dev/null ${healthUrl}", returnStatus: true) == 0) {
                healthy = true
                break
            }
            sleep(retryInterval)
        }

        if (!healthy) {
            String logs = sh(script: "docker logs ${containerName} 2>&1 || true", returnStdout: true).trim()
            error "containerValidate: Health check failed at '${healthUrl}'.\n--- Logs ---\n${logs}"
        }

        echo "  CONTAINER SMOKE TEST PASSED — Application is healthy."

    } finally {
        sh "docker rm -f ${containerName} 2>/dev/null || true"
    }
}