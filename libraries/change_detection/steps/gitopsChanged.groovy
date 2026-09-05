// steps/gitopsChanged.groovy — True if gitops/** changed.
//
// Note: app_ci and promotion also modify gitops/** themselves as part of
// their own run — this predicate is about *incoming* changes at the start
// of a build, not changes the pipeline makes to itself mid-run.

boolean call() {
    List<String> files = changedFiles()
    List<String> patterns = config.gitops_paths ?: ['gitops/**', 'helm/**']
    boolean result = matchesAny(files, patterns)
    echo "change_detection/gitopsChanged: ${result}"
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
