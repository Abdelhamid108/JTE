// docker/steps/containerValidate.groovy
//
// Binds an ephemeral host port dynamically (-p 0:${containerPort}) to prevent
// port collisions across concurrent builds on the same Jenkins agent node.

void call(Map args = [:]) {
    String image         = args.image_uri ?: env.IMAGE_URI ?: env.PIPELINE_IMAGE
    String containerName = "validate-${env.BUILD_ID}"
    int containerPort    = (config.container_port ?: 8080) as Integer
    String healthPath    = config.health_check_path ?: '/actuator/health'
    int maxRetries       = 12
    int retryInterval    = 5

    if (!image) {
        error "containerValidate: No image specified or available in env.IMAGE_URI."
    }

    echo "═══════════════════════════════════════════"
    echo "  CONTAINER SMOKE TEST — ${image}"
    echo "═══════════════════════════════════════════"

    try {
        // Run with dynamic ephemeral host port
        sh "docker run -d --name ${containerName} -p 0:${containerPort} ${image}"

        // Extract assigned host port cleanly across IPv4/IPv6 Docker bindings
        String hostPort = sh(
            script: "docker port ${containerName} ${containerPort}/tcp | head -n 1 | awk -F: '{print \$NF}'",
            returnStdout: true
        ).trim()

        if (!hostPort) {
            error "containerValidate: Could not resolve dynamically-assigned host port for ${containerName}."
        }

        String healthUrl = "http://localhost:${hostPort}${healthPath}"
        echo "  Health Endpoint: ${healthUrl} (host port ${hostPort} -> container port ${containerPort})"

        boolean healthy = false
        for (int i = 1; i <= maxRetries; i++) {
            echo "containerValidate: Health check attempt ${i}/${maxRetries}..."
            int status = sh(
                script: "curl -s -f -o /dev/null ${healthUrl}",
                returnStatus: true
            )
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

        echo "  CONTAINER SMOKE TEST PASSED — Application is healthy."

    } finally {
        sh "docker stop ${containerName} 2>/dev/null || true"
        sh "docker rm -f ${containerName} 2>/dev/null || true"
    }
}
