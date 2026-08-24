// steps/validateVersion.groovy — Enforce the accepted version format.
//
// Contract:
//   input : a version string (defaults to env.APP_VERSION, set by
//           version_manager/readVersion)
//   output: the validated version string, unchanged
//   fails : any version that does not match config.version_regex

String call(Map args = [:]) {
    String version = args.version ?: env.APP_VERSION
    String pattern = config.version_regex ?: /^\d+\.\d+\.\d+(-[0-9A-Za-z.-]+)?$/

    if (!version) {
        error "release/validateVersion: no version to validate (readVersion must run first)."
    }
    if (!(version ==~ pattern)) {
        error "release/validateVersion: '${version}' does not match the required format (${pattern})."
    }

    echo "release/validateVersion: '${version}' is valid."
    return version
}
