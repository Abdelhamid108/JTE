// steps/checkov.groovy
//
// Policy: Checkov is a HARD/BLOCKING security gate by default. Set
// config.softFail = true to explicitly downgrade it to warning-only for a
// specific pipeline/environment (e.g. while iterating on new modules) —
// this must be a deliberate, visible override, not the default.

void call() {
    String targetDir = config.infra_dir ?: '.'
    String softFail  = config.softFail ? '--soft-fail' : ''

    if (config.softFail) {
        echo "checkov: WARNING — softFail=true, Checkov findings will NOT block this pipeline."
    }

    echo "Executing Checkov tests..."
    dir(targetDir) {
        sh """
            export PATH="\${HOME}/.local/bin:/usr/local/bin:\$PATH"
            checkov -d . --framework terraform ${softFail}
        """
    }
}
