// library_config.groovy — Maven library configuration schema

fields {
    required {
    }
    optional {
        app_dir               = String   // Default: '.'
        maven_command         = String   // Default: 'mvn'
        skip_tests_on_package = Boolean  // Default: true
        junit_report_glob     = String   // Default: '**/target/surefire-reports/*.xml'
    }
}

steps {
    test
    packageApp
    verify
}
