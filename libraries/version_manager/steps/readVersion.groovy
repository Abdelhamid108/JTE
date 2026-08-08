
String call(Map args = [:]) {
    String appDir      = args.app_dir      ?: config.app_dir      ?: '.'
    String versionFile = args.version_file ?: config.version_file ?: 'VERSION'

    String version = null

    dir(appDir) {
        if (!fileExists(versionFile)) {
            error "readVersion: File '${versionFile}' not found."
        }

        if (versionFile.endsWith('.json')) {
            String content = readFile(file: versionFile)
            def json = readJSON text: content
            version = json.version
        } else {
            version = readFile(file: versionFile).trim()
        }
    }

    if (!version?.trim()) {
        error "readVersion: No version found in '${versionFile}'."
    }

    version = version.trim()
    env.APP_VERSION = version
    echo "readVersion: ${version} (from ${versionFile})"
    return version
}
