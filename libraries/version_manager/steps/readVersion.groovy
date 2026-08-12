String call(Map args = [:]) {
    String appDir      = args.app_dir      ?: config.app_dir      ?: '.'
    String versionFile = args.version_file ?: config.version_file ?: 'VERSION'

    String targetFile  = (appDir != '.') ? "${appDir}/${versionFile}" : versionFile

    String version = readFile(file: targetFile).trim()

    env.APP_VERSION = version
    echo "readVersion: ${version} (from ${targetFile})"
    return version
}
