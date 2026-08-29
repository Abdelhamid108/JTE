// steps/changedFiles.groovy — Compute the set of files changed by this build.
//
// Contract:
//   output: List<String> of changed file paths (repo-relative), also cached
//           in env.CHANGED_FILES (comma-joined) so the other predicates in
//           this library don't recompute the diff on every call.
//
// Handles:
//   - first build on a branch (no previous successful build)   -> treat
//     everything as changed, so nothing is silently skipped
//   - pull request build (env.CHANGE_TARGET is set)              -> diff
//     against the PR's target branch
//   - normal branch/merge build                                  -> diff
//     against the previous commit

List call() {
    if (env.CHANGED_FILES != null) {
        return env.CHANGED_FILES.split(',').findAll { it }
    }

    String baseBranch = config.base_branch ?: 'main'
    List<String> files = []

    if (env.CHANGE_TARGET) {
        echo "change_detection/changedFiles: PR build, diffing against origin/${env.CHANGE_TARGET}"
        sh "git fetch origin ${env.CHANGE_TARGET}"
        String diff = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}...HEAD", returnStdout: true).trim()
        files = diff ? diff.split('\n') as List : []
    } else {
        int hasPreviousCommit = sh(script: 'git rev-parse HEAD~1', returnStatus: true)
        if (hasPreviousCommit == 0) {
            echo "change_detection/changedFiles: diffing HEAD~1..HEAD"
            String diff = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()
            files = diff ? diff.split('\n') as List : []
        } else {
            echo "change_detection/changedFiles: no previous commit found (first build) — treating all files as changed."
            String all = sh(script: 'git ls-files', returnStdout: true).trim()
            files = all ? all.split('\n') as List : []
        }
    }

    env.CHANGED_FILES = files.join(',')
    echo "change_detection/changedFiles: ${files.size()} file(s) changed."
    return files
}
