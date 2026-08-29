// docker/steps/containerValidate.groovy — Validates container health with dynamic port allocation

void call(Map args = [:]) {
    String containerName = "validate-${env.BUILD_ID}"
    int containerPort    = config.container_port as Integer
    String healthPath    = config.health_check_path
    String image         = env.IMAGE_URI
    int maxRetries       = 12
    int retryInterval    = 5

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

        echo "  CONTAINER SMOKE TEST PASSED — Application is healthy."

    } finally {
        sh "docker stop ${containerName} 2>/dev/null || true"
        sh "docker rm -f ${containerName} 2>/dev/null || true"
    }
}
