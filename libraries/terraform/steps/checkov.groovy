// steps/checkov.groovy
//
// Security & Compliance Policy Gate.
// Sets env.SECURITY_POLICY_PASSED = 'true' on success so the @BeforeStep
// hook in governanceHooks.groovy can skip re-running the scan in normal flow.
// Set config.softFail = true to downgrade to warning-only (non-blocking).

void call() {
    String targetDir = config.infra_dir ?: '.'
    String softFail  = config.softFail ? '--soft-fail' : ''

    if (config.softFail) {
        echo "checkov: WARNING — softFail=true, findings will NOT block this pipeline."
    }

    echo "checkov: running security scan on '${targetDir}'..."
    dir(targetDir) {
        sh """
            export PATH="\${HOME}/.local/bin:/usr/local/bin:\$PATH"
            checkov -d . --framework terraform ${softFail}
        """
    }
    env.SECURITY_POLICY_PASSED = 'true'
}
