// docker/steps/containerValidate.groovy — Validates container health with dynamic port allocation

void call(Map args = [:]) {
    String containerName = "validate-${env.BUILD_ID}"
    int containerPort    = (args.container_port ?: config.container_port ?: 8080) as Integer
    String healthPath    = args.health_check_path ?: config.health_check_path ?: "/actuator/health"
    String image         = args.image ?: env.IMAGE_URI ?: env.PIPELINE_IMAGE
    int maxRetries       = (args.max_retries ?: (config.validate_wait_seconds ? (config.validate_wait_seconds / 5) : 12)) as Integer
    int retryInterval    = 5

    if (!image) {
        error "docker/containerValidate: No image specified and neither IMAGE_URI nor PIPELINE_IMAGE is set."
    }

    echo "═══════════════════════════════════════════"
    echo "  CONTAINER SMOKE TEST — ${image}"
    echo "═══════════════════════════════════════════"

    try {
        sh "docker run -d --name ${containerName} -p 0:${containerPort} ${image}"

        String hostPort = sh(
            script: "docker port ${containerName} ${containerPort}/tcp | head -n 1 | awk -F: '{print \$NF}'",
            returnStdout: true
        ).trim()

        String healthUrl = "http://localhost:${hostPort}${healthPath}"
        echo "  Health Endpoint: ${healthUrl} (host port ${hostPort} -> container port ${containerPort})"

        boolean healthy = false
        for (int i = 1; i <= maxRetries; i++) {
            echo "containerValidate: Attempt ${i}/${maxRetries}..."
            int status = sh(script: "curl -s -f -o /dev/null ${healthUrl}", returnStatus: true)
            if (status == 0) {
                healthy = true
                break
            }
            sleep(retryInterval)
        }

        if (!healthy) {
            String logs = sh(script: "docker logs ${containerName} 2>&1 || true", returnStdout: true).trim()
            error "containerValidate: Health check failed at '${healthUrl}'.\n--- Container Logs ---\n${logs}"
        }

        env.STAGE_CONTAINER_VALIDATE_PASSED = 'true'
        echo "  CONTAINER SMOKE TEST PASSED — Application is healthy."

    } finally {
        sh "docker stop ${containerName} 2>/dev/null || true"
        sh "docker rm -f ${containerName} 2>/dev/null || true"
    }
}
