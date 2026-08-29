// steps/readVersion.groovy — Reads version from configured file

String call(Map args = [:]) {
    String appDir      = args.app_dir      ?: config.app_dir      ?: '.'
    String versionFile = args.version_file ?: config.version_file ?: 'VERSION'

    String targetFile = (appDir != '.') ? "${appDir}/${versionFile}" : versionFile

    if (!fileExists(targetFile)) {
        error "version_manager/readVersion: Version file '${targetFile}' not found."
    }

    String content = readFile(file: targetFile).trim()
    String version = versionFile.endsWith('.json') ? (readJSON(text: content).version) : content

    if (!version?.trim()) {
        error "version_manager/readVersion: No version string found in '${targetFile}'."
    }

    version = version.trim()
    env.APP_VERSION = version
    echo "version_manager/readVersion: ${version} (from ${targetFile})"
    return version
}
