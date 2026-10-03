import org.devops.Docker

/**
 * Inspect a Docker image or container and return the output.
 *
 * Usage:
 *   dockerInspect target: 'myapp:latest'
 *   dockerInspect target: 'myapp:latest', format: '{{.Id}}'
 *
 * @param config  map with:
 *   - target  : image or container name/tag (required)
 *   - format  : output format (optional, default '{{json .}}')
 */
def call(Map config = [:]) {
  def target = config.target
  if (!target) {
    error('dockerInspect: "target" parameter is required')
  }

  def docker = new Docker(this)
  return docker.inspect(config)
}
