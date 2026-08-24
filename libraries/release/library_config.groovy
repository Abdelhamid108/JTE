// library_config.groovy — Release library configuration schema
//
// NOTE ON SCOPE (deviation from the original 3-step list): the plan's own
// Step 9.1 says "Use the version-manager functionality rather than
// duplicating version parsing." version_manager/readVersion already reads
// and exports APP_VERSION, and this library is always loaded alongside
// version_manager. Re-declaring a 'readVersion' step here would collide
// with that step name in JTE's shared step namespace, so this library does
// NOT re-implement version reading — it calls the global readVersion()
// step provided by version_manager and focuses only on release-specific
// concerns: format validation and tagging.

fields {
    required {
        git_creds = String  // Jenkins credential (username/password or token) used to push tags
    }
    optional {
        tag_prefix    = String   // Default: 'v'  -> tags look like v1.0.1
        version_regex = String   // Default: semantic versioning, e.g. 1.0.1 or 1.0.1-rc.1
    }
}

steps {
    validateVersion
    createTag
}
