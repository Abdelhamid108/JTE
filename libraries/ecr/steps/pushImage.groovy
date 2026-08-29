// steps/pushImage.groovy — Push the built image to ECR.
//
// Contract:
//   input : env.IMAGE_URI (set by ecr/buildImage), a passing trivy/scanImage
//   output: image pushed to ECR under its immutable version tag
//   fails : missing IMAGE_URI, or a failed docker push
//
// Callers must only invoke this step after Maven, Sonar policy, the Docker
// build, and the Trivy image scan have all succeeded — this library does
// not itself re-check that ordering, the pipeline template does.

void call() {
    if (!env.IMAGE_URI) {
        error "ecr/pushImage: IMAGE_URI is not set. Run ecr/buildImage first."
    }

    echo "ecr/pushImage: pushing ${env.IMAGE_URI}"
    sh "docker push ${env.IMAGE_URI}"
}
