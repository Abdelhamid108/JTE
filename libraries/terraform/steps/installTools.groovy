// steps/installTools.groovy — Install Terraform pipeline agent dependencies.
//
// Per team decision, tool installation stays dynamic (not baked into a
// prebuilt agent image) for tools that are not already guaranteed to be on
// the agent. Terraform and the AWS CLI are assumed to be present on the
// agent already; this step is responsible for Checkov and its Python
// prerequisites, which are the tools most likely to drift/be missing.

void call(Map args = [:]) {
    echo "Installing Checkov and agent dependencies inside container..."
    sh '''
        if command -v apk >/dev/null 2>&1; then
            apk update && apk add --no-cache python3 py3-pip git && pip3 install checkov --break-system-packages
        else
            apt-get update && apt-get install -y python3 python3-pip git && pip3 install checkov --break-system-packages || true
        fi
    '''
}
