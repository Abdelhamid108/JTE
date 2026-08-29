// library_config.groovy — Maven library configuration schema
//
// Responsible for building/testing the Spring PetClinic application
// (application/) with the Maven wrapper.

fields {
    required {
    }
    optional {
        app_dir            = String   // Default: 'application'
        maven_command       = String   // Default: './mvnw'
        skip_tests_on_package = Boolean // Default: false — package should NOT silently skip tests
        junit_report_glob  = String   // Default: '**/target/surefire-reports/*.xml'
    }
}

steps {
    test
    packageApp
    verify
}
