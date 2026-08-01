// steps/approval.groovy

def call (Map config = [:]){
    def action = config.action ?: 'Deploy'

    timeout(time: 10, unit: 'MINUTES'){
        input message:"Approve Terraform ${action} ?", ok: "proceed"
    }
}
