// library_config.groovy — GitOps library configuration schema
//
// The GitOps tree (gitops/dev, gitops/test, gitops/prod values.yaml) lives
// inside the same 'petclinic-platform' monorepo as this Jenkinsfile, so no
// separate repository checkout is performed by this library — it edits and
// commits files in the *current* workspace.
//
// Dev values are updated and committed directly by app_ci. Test/prod values
// are only ever changed through a promotion pull request (never a direct
// push) — see createPromotionPR.

fields {
    required {
        git_creds = String  // Jenkins credential (username/password or token) for push + PR creation
    }
    optional {
        gitops_branch      = String   // Default: 'main' — base/target branch for direct commits
        values_path_template = String // Default: 'gitops/${env}/values.yaml'
        git_user_name        = String // Default: 'jenkins-jte'
        git_user_email       = String // Default: 'jenkins-jte@petclinic-platform.local'
        pr_repo_slug          = String // e.g. 'org/petclinic-platform', required for createPromotionPR
    }
}

steps {
    updateValues
    commitChanges
    createPromotionPR
}
