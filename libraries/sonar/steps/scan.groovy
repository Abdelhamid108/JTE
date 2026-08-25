// steps/scan.groovy — Run SonarCloud analysis and (optionally) enforce the gate.
//
// Contract:
//   input : compiled/verified application code (run maven/verify first)
//   output: analysis published to SonarCloud; env.SONAR_QUALITY_GATE_STATUS
//   fails : only if config.enforce_quality_gate == true and the gate fails,
//           or if the scanner itself cannot execute (auth/network failure)
//
// Credentials: the Sonar token is read from a Jenkins credential
// (config.sonar_credentials_id) and is never echoed or written to disk.

void call(Map args = [:]) {
    String appDir       = args.app_dir       ?: config.app_dir       ?: 'application'
    String mavenCommand = args.maven_command ?: config.maven_command ?: './mvnw'
    String project      = config.sonar_project
    String org          = config.sonar_organization ?: ''
    String hostUrl      = config.sonar_host_url ?: ''
    String credsId      = config.sonar_credentials_id
    boolean enforce     = config.enforce_quality_gate ? config.enforce_quality_gate.toString().toBoolean() : false
    int timeoutMinutes  = (config.quality_gate_timeout_minutes ?: '10') as Integer

    if (!project) {
        error "sonar/scan: 'sonar_project' is required."
    }

    String orgArg  = org     ? "-Dsonar.organization=${org}" : ''
    String hostArg = hostUrl ? "-Dsonar.host.url=${hostUrl}" : ''

    def runScan = {
        dir(appDir) {
            sh """
                ${mavenCommand} -B sonar:sonar \
                -Dsonar.projectKey=${project} \
                ${orgArg} \
                ${hostArg} \
                -Dsonar.token=\$SONAR_TOKEN
            """
        }
    }

    if (credsId) {
        withCredentials([
            string(credentialsId: credsId, variable: 'SONAR_TOKEN')
        ]) {
            runScan()
        }
    } else {
        error "sonar/scan: sonar_credentials_id is required."
    }
    
    if (enforce) {
        echo "sonar/scan: enforce_quality_gate=true — waiting for the SonarCloud quality gate (timeout ${timeoutMinutes}m)."
        timeout(time: timeoutMinutes, unit: 'MINUTES') {
            def qualityGate = waitForQualityGate()
            env.SONAR_QUALITY_GATE_STATUS = qualityGate.status
            if (qualityGate.status != 'OK') {
                error "sonar/scan: Quality gate failed with status '${qualityGate.status}'."
            }
        }
    } else {
        echo "sonar/scan: quality gate is non-blocking by policy (enforce_quality_gate=false). Review results in SonarCloud."
    }
}
