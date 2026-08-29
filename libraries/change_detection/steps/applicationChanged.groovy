// steps/applicationChanged.groovy — True if application/** (or JTE/**) changed.
//
// JTE/** is included because a pipeline-behavior change can affect how the
// application pipeline itself runs, so it must not be silently skipped.

boolean call() {
    List<String> files = changedFiles()
    List<String> patterns = (config.application_paths ?: ['application/**']) + (config.jte_paths ?: ['JTE/**'])
    boolean result = matchesAny(files, patterns)
    echo "change_detection/applicationChanged: ${result}"
    return result
}

private boolean matchesAny(List<String> files, List<String> patterns) {
    return files.any { file -> patterns.any { pattern -> matches(file, pattern) } }
}

private boolean matches(String file, String pattern) {
    String regex = pattern
        .replace('.', '\\.')
        .replace('**', '§§')
        .replace('*', '[^/]*')
        .replace('§§', '.*')
    return file ==~ "^${regex}\$"
}
