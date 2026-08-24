// steps/terraformChanged.groovy — True if terraform/** (or JTE/**) changed.

boolean call() {
    List<String> files = changedFiles()
    List<String> patterns = (config.terraform_paths ?: ['terraform/**']) + (config.jte_paths ?: ['JTE/**'])
    boolean result = matchesAny(files, patterns)
    echo "change_detection/terraformChanged: ${result}"
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
