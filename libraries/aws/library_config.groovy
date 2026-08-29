// library_config.groovy — AWS library configuration schema
//
// Assumes an AWS IAM role using temporary credentials.
// The Jenkins credential contains the base credentials for an IAM
// identity that is allowed to perform sts:AssumeRole.
//
// The assumed role credentials are then made available to subsequent
// AWS operations such as Terraform, ECR, and AWS CLI commands.

fields {

    required {

        aws_credentials_id = String
        aws_role_arn       = String
        aws_region         = String

    }

    optional {

        role_session_name = String
        role_duration     = Integer

    }
}

steps {

    assumeRole

}