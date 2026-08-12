// library_config.groovy — NPM library configuration schema

fields {
    required {
    }
    optional {
        app_dir       = String    // Directory containing package.json. Default: '.'
        skip_build    = Boolean   // true = skip the build step entirely. Default: false
        install_tools = Boolean   // true = install agent dependencies (aws-cli, kubectl, etc.). Default: false
    }
}

steps {
    installDeps
    npmInstallTools
    buildApp
    npmLint
    testApp
    audit
}
