// steps/createTag.groovy — Create and push an immutable Git release tag.
//
// Contract:
//   input : a validated version string (run release/validateVersion first)
//   output: a 'v<version>' tag pushed to origin
//   fails : the tag already exists (duplicate release), or a push failure
//
// A duplicate tag is treated as an existing release and blocks the
// pipeline rather than silently overwriting history.

void call(Map args = [:]) {
    String version   = args.version ?: env.APP_VERSION
    String prefix     = config.tag_prefix ?: 'v'
    String gitCreds   = config.git_creds

    if (!version) { error "release/createTag: no version available. Run readVersion + validateVersion first." }
    if (!gitCreds) { error "release/createTag: 'git_creds' is required." }

    String tag = "${prefix}${version}"

    int exists = sh(script: "git ls-remote --tags origin refs/tags/${tag} | grep -q .", returnStatus: true)
    if (exists == 0) {
        error "release/createTag: tag '${tag}' already exists. Refusing to create a duplicate release."
    }

    sh "git tag ${tag}"

    withCredentials([usernamePassword(credentialsId: gitCreds, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
        String remote = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()
        String authedRemote = remote.replaceFirst('https://', "https://${env.GIT_USER}:${env.GIT_TOKEN}@")
        sh "git push ${authedRemote} ${tag}"
    }

    echo "release/createTag: pushed tag '${tag}'"
}
