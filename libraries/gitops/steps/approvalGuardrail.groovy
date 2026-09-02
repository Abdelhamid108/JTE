// steps/approvalGuardrail.groovy — Interactive Approval Guardrail with Graceful Skip Support

boolean call(Map args = [:]) {
    String environment = args.environment ?: "TEST"
    int timeoutHours   = args.timeout_hours ?: 24
    String message     = args.message ?: "Approve deployment to ${environment} Environment?"
    String submitter   = args.submitter ?: ""

    echo "gitops/approvalGuardrail: Prompting approval for environment '${environment}' (Timeout: ${timeoutHours}h)..."

    try {
        timeout(time: timeoutHours, unit: 'HOURS') {
            Map inputParams = [
                message: message,
                ok: "Submit Decision",
                parameters: [
                    choice(
                        name: 'ACTION',
                        choices: ["Deploy to ${environment}", "Skip ${environment} & Finish"],
                        description: "Choose whether to promote to ${environment} or finish pipeline without deploying."
                    )
                ]
            ]
            if (submitter) {
                inputParams.submitter = submitter
            }

            def userChoice = input(inputParams)
            boolean proceed = (userChoice == "Deploy to ${environment}")

            if (proceed) {
                echo "gitops/approvalGuardrail: User approved deployment to ${environment}."
            } else {
                echo "gitops/approvalGuardrail: User selected 'Skip ${environment} & Finish'. Pipeline will complete cleanly."
            }
            return proceed
        }
    } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
        echo "gitops/approvalGuardrail: Approval was aborted or timed out. Skipping ${environment} without failing build."
        return false
    } catch (Exception e) {
        echo "gitops/approvalGuardrail: Warning: ${e.message}. Skipping ${environment}."
        return false
    }
}
