    // steps/approval.groovy

void call(Map args = [:]) {
    boolean isDestroy = args.is_destroy != null ? args.is_destroy.toString().toBoolean() : (config.is_destroy ? config.is_destroy.toString().toBoolean() : false)
    String action = isDestroy ? "DESTROY Infrastructure" : "DEPLOY Infrastructure"

    input message: "Approve Terraform ${action} ?", ok: "proceed"
}
