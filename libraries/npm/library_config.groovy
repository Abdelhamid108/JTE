// library_config.groovy — NPM library configuration schema

fields {
    required {
        // No strictly required fields — all have sensible defaults
    }
    optional {
        app_dir       = String    // Directory containing package.json. Default: '.'
        skip_lint     = Boolean   // true = skip the linter step entirely. Default: false
        install_tools = Boolean   // true = install agent dependencies (aws-cli, kubectl, etc.). Default: false
    }
}

steps {
    installDeps
    installTools
    buildApp
    lint
    testApp
    audit
}
