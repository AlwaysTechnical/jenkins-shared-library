import org.devops.Docker

/**
 * Execute a docker-compose command.
 *
 * Usage:
 *   dockerCompose command: 'up', args: '-d'
 *   dockerCompose command: 'down', composeFile: 'docker-compose.prod.yml'
 *   dockerCompose command: 'build', env: [BUILD_NUMBER: env.BUILD_NUMBER]
 *
 * @param config  map with:
 *   - command     : compose command (up, down, build, etc.) (required)
 *   - composeFile : path to docker-compose file (default 'docker-compose.yml')
 *   - args        : additional compose arguments (optional)
 *   - env         : map of environment variables (optional)
 *   - retryAttempts     : retry count (default 1)
 *   - retryDelaySeconds : retry delay in seconds (default 0)
 */
def call(Map config = [:]) {
  def command = config.command
  if (!command) {
    error('dockerCompose: "command" parameter is required')
  }

  def docker = new Docker(this)
  docker.compose(config)
}