// steps/buildImage.groovy — Build the application Docker image.
//
// Contract:
//   input : APP_VERSION (env, set by version_manager/readVersion),
//           application/Dockerfile
//   output: env.IMAGE_NAME, env.IMAGE_TAG, env.IMAGE_URI
//   fails : missing APP_VERSION, missing ECR_REGISTRY (run ecr/login first),
//           or a failed docker build
//
// The application version is always used as the immutable image tag.
// 'latest' is never used as a deployment identity.

void call(Map args = [:]) {
    String dockerfile = args.dockerfile_path ?: config.dockerfile_path ?: 'application/Dockerfile'
    String context     = args.build_context   ?: config.build_context   ?: 'application'
    String imageName   = args.image_name      ?: config.image_name      ?: config.ecr_repository
    String version     = args.version         ?: env.APP_VERSION

    if (!version)          { error "ecr/buildImage: APP_VERSION is not set. Run version_manager/readVersion first." }
    if (!env.ECR_REGISTRY) { error "ecr/buildImage: ECR_REGISTRY is not set. Run ecr/login first." }
    if (!imageName)        { error "ecr/buildImage: 'image_name' (or 'ecr_repository') is required." }

    String imageUri = "${env.ECR_REGISTRY}/${imageName}:${version}"

    echo "ecr/buildImage: building ${imageUri} from ${dockerfile}"
    sh "docker build -f ${dockerfile} -t ${imageUri} ${context}"

    env.IMAGE_NAME = imageName
    env.IMAGE_TAG  = version
    env.IMAGE_URI  = imageUri
}
