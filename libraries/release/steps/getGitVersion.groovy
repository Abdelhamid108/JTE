void call(Map args = [:]) {
    // 1. Resolve short commit SHA purely from native Jenkins environment variable
    env.GIT_SHORT_SHA = env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'latest'

    // 2. Resolve version tag based on native Jenkins Multibranch environment variables
    if (env.TAG_NAME) {
        // Tag build (e.g. v1.0.0, v1.0.0-rc1)
        env.GIT_TAG = env.TAG_NAME
        echo "release/getGitVersion: Tag build detected from env.TAG_NAME: ${env.GIT_TAG}"
    } else if (env.CHANGE_ID) {
        // Pull Request build (e.g. PR-5)
        env.GIT_TAG = "pr-${env.CHANGE_ID}-${env.GIT_SHORT_SHA}"
        echo "release/getGitVersion: PR build detected from env.CHANGE_ID: ${env.GIT_TAG}"
    } else {
        // Branch build (e.g. main)
        env.GIT_TAG = env.GIT_SHORT_SHA
        echo "release/getGitVersion: Branch build on '${env.BRANCH_NAME ?: 'unknown'}'. Using commit SHA: ${env.GIT_TAG}"
    }

    echo "release/getGitVersion: Active GIT_TAG='${env.GIT_TAG}' (commit: ${env.GIT_SHORT_SHA})"
}