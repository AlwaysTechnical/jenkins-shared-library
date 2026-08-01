import org.devops.Docker

/**
 * Login to AWS Elastic Container Registry (ECR).
 *
 * Usage:
 *   dockerLoginEcr region: 'us-east-1', accountId: '123456789012'
 *   dockerLoginEcr region: 'us-east-1', registry: '123456789012.dkr.ecr.us-east-1.amazonaws.com', credentialsId: 'aws-creds'
 *
 * @param config  map with:
 *   - region        : AWS region (required)
 *   - accountId     : AWS account ID (optional, used to derive registry)
 *   - registry      : ECR registry URI (optional, auto-derived if accountId provided)
 *   - credentialsId : Jenkins credentials ID for AWS access keys (optional)
 */
def call(Map config = [:]) {
  def region = config.region
  if (!region) {
    error('dockerLoginEcr: "region" parameter is required')
  }

  def docker = new Docker(this)
  docker.loginEcr(config)
}