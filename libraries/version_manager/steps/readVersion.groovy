
String call(Map args = [:]) {
    String appDir      = args.app_dir      ?: config.app_dir      ?: '.'
    String versionFile = args.version_file ?: config.version_file ?: 'VERSION'

    String targetFile = (appDir != '.') ? "${appDir}/${versionFile}" : versionFile

    if (!fileExists(targetFile)) {
        error "readVersion: File '${targetFile}' not found."
    }

    String content = readFile(file: targetFile).trim()
    String version = ''

    if (versionFile.endsWith('.json')) {
        def json = readJSON text: content
        version = json.version
    } else {
        version = content
    }

    if (!version?.trim()) {
        error "readVersion: No version found in '${targetFile}'."
    }

    version = version.trim()
    env.APP_VERSION = version
    echo "readVersion: ${version} (from ${targetFile})"
    return version
}
