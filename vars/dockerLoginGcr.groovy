import org.devops.Docker

/**
 * Login to Google Container Registry (GCR).
 *
 * Usage:
 *   dockerLoginGcr credentialsId: 'gcr-service-account'
 *   dockerLoginGcr project: 'my-gcp-project'
 *   dockerLoginGcr registry: 'us.gcr.io', credentialsId: 'gcr-service-account'
 *
 * @param config  map with:
 *   - credentialsId : Jenkins credentials ID for GCR service account key (optional)
 *   - project       : GCP project ID (optional)
 *   - registry      : GCR registry URL (default 'gcr.io')
 */
def call(Map config = [:]) {
  def docker = new Docker(this)
  docker.loginGcr(config)
}