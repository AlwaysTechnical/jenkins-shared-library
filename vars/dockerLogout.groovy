import org.devops.Docker

/**
 * Logout from a Docker registry.
 *
 * Usage:
 *   dockerLogout registry: 'registry.example.com'
 *   dockerLogout
 *
 * @param config  map with:
 *   - registry : registry URL (default Docker Hub)
 */
def call(Map config = [:]) {
  def docker = new Docker(this)
  docker.logout(config)
}
