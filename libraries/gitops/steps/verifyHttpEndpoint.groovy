// steps/verifyHttpEndpoint.groovy — Verify health and version feedback from deployed endpoint

void call(Map args = [:]) {
    String url         = args.url
    String expectedTag = args.expected_tag
    int timeoutMin     = args.timeout ?: 5

    if (!url) {
        echo "gitops/verifyHttpEndpoint: No URL specified, skipping endpoint verification."
        return
    }

    echo "gitops/verifyHttpEndpoint: Probing ${url} (Expected Tag: ${expectedTag ?: 'N/A'}, Timeout: ${timeoutMin}m)..."

    timeout(time: timeoutMin, unit: 'MINUTES') {
        waitUntil {
            // Check HTTP status code
            int statusCode = sh(
                script: "curl -s -o /dev/null -w '%{http_code}' '${url}' || echo '000'",
                returnStdout: true
            ).trim() as Integer

            echo "Health probe status code: ${statusCode}"

            if (statusCode == 200 || statusCode == 302) {
                if (expectedTag) {
                    // Optional version-aware check if /actuator/info returns the tag
                    String infoBody = sh(
                        script: "curl -s '${url}/actuator/info' || curl -s '${url}/actuator/health' || echo ''",
                        returnStdout: true
                    ).trim()

                    if (infoBody.contains(expectedTag)) {
                        echo "SUCCESS: Endpoint is running the NEW image tag '${expectedTag}'!"
                        return true
                    } else {
                        echo "Endpoint responded ${statusCode}, but tag '${expectedTag}' not yet active. Waiting..."
                        sleep 5
                        return false
                    }
                }
                return true
            }

            sleep 5
            return false
        }
    }
    echo "gitops/verifyHttpEndpoint: Endpoint ${url} is LIVE and HEALTHY!"
}
