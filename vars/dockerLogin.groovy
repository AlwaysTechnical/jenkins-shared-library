import org.devops.Docker

/**
 * Login to a Docker registry.
 *
 * Usage:
 *   dockerLogin registry: 'registry.example.com', credentialsId: 'docker-cred'
 *   dockerLogin registry: 'registry.example.com', username: 'user', password: 'pass'
 *
 * @param config  map with:
 *   - registry          : registry URL (required)
 *   - username          : registry username (optional, uses credentials if provided)
 *   - password          : registry password (optional, uses credentials if provided)
 *   - credentialsId     : Jenkins credentials ID for registry auth
 */
def call(Map config = [:]) {
  def registry = config.registry
  if (!registry) {
    error('dockerLogin: "registry" parameter is required')
  }

  def docker = new Docker(this)
  docker.login(config)
}
