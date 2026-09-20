import org.devops.Docker

/**
 * Push a Docker image tag to a registry.
 *
 * Usage:
 *   dockerPush image: 'myapp', registry: 'registry.example.com', tag: '1.0.0'
 *   dockerPush image: 'myapp', registry: 'registry.example.com', registryCredentialsId: 'docker-cred'
 *
 * @param config  map with:
 *   - image             : image name (required)
 *   - tag               : tag to push (default 'latest')
 *   - registry          : registry URL (required for push)
 *   - registryCredentialsId : credentials for registry login (optional)
 *   - retryAttempts     : retry count (default 1)
 *   - retryDelaySeconds : retry delay in seconds (default 0)
 */
def call(Map config = [:]) {
  def image = config.image
  if (!image) {
    error('dockerPush: "image" parameter is required')
  }

  def registry = config.registry
  if (!registry) {
    error('dockerPush: "registry" parameter is required')
  }

  def docker = new Docker(this)
  docker.push(config)
}
