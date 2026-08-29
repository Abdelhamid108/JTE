// library_config.groovy — Change detection library configuration schema
//
// The monorepo has independent pipelines (app_ci, terraform_ci) that must
// not run their full, expensive workflow when nothing relevant to them
// changed. This library computes the changed-file set once per build and
// exposes path-based predicates for the pipeline templates to branch on.

fields {
    required {
    }
    optional {
        application_paths = List   // Default: ['application/**']
        terraform_paths    = List   // Default: ['terraform/**']
        gitops_paths        = List   // Default: ['gitops/**']
        jte_paths            = List   // Default: ['JTE/**']
        base_branch          = String // Default: 'main' — diff target for PR/merge builds
    }
}

steps {
    changedFiles
    applicationChanged
    terraformChanged
    gitopsChanged
}
