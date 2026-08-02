    // steps/approval.groovy

    void call (Map config = [:]){
    boolean isDestroy = config.is_destroy ? config.is_destroy.toString().toBoolean() : false  
    String action = isDestroy ? "DESTROY Infrastructure" : "DEPLOY Infrastructure"

    input message:"Approve Terraform ${action} ?", ok: "proceed"
        
    }
