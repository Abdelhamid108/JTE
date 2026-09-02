void call(Map args = [:]) {
    // Strictly retrieve exact Git tag on current commit
    String tag = sh(
        script: "git describe --tags --exact-match 2>/dev/null || echo ''",
        returnStdout: true
    ).trim()

    if (!tag) {
        error "STRICT VERSION FAILURE: No Git tag found on this commit! Every build/PR must be tagged (e.g., 'git tag v1.0.0 && git push origin v1.0.0')."
    }

    env.GIT_TAG = tag
    echo "Resolved strict Git tag: ${env.GIT_TAG}"
}