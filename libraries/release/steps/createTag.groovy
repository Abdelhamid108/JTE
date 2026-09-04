// release/steps/createTag.groovy — Git release tag creator

void call(Map args = [:]) {
    String prefix  = config.tag_prefix ?: 'v'
    String credsId = config.git_creds
    String tag     = "${prefix}${env.APP_VERSION}"

    echo "release/createTag: Creating tag '${tag}'..."

    int exists = sh(script: "git ls-remote --tags origin refs/tags/${tag} | grep -q .", returnStatus: true)
    if (exists == 0) {
        echo "release/createTag: Tag '${tag}' already exists on remote. Skipping push."
        return
    }

    sh "git tag ${tag}"
    withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
        String remote = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()
        String authedRemote = remote.replaceFirst('https://', "https://${env.GIT_USER}:${env.GIT_TOKEN}@")
        sh "git push ${authedRemote} ${tag}"
    }
    echo "release/createTag: Tag '${tag}' pushed successfully."
}
