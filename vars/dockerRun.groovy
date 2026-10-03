import org.devops.Docker

/**
 * Run a Docker container.
 *
 * Usage:
 *   dockerRun image: 'myapp', command: 'npm start', detached: true
 *   dockerRun image: 'myapp', ports: ['8080:8080'], volumes: ['/data:/app/data']
 *   dockerRun image: 'myapp', env: [NODE_ENV: 'production'], rm: true
 *
 * @param config  map with:
 *   - image     : image to run (required)
 *   - command   : command to run inside the container (optional)
 *   - args      : additional docker run flags (optional)
 *   - detached  : run in detached mode (default false)
 *   - env       : map of environment variables (optional)
 *   - ports     : list of port mappings (optional)
 *   - volumes   : list of volume mounts (optional)
 *   - rm        : remove container after exit (default true)
 *   - name      : container name (optional)
 *   - retryAttempts     : retry count (default 1)
 *   - retryDelaySeconds : retry delay in seconds (default 0)
 */
def call(Map config = [:]) {
  def image = config.image
  if (!image) {
    error('dockerRun: "image" parameter is required')
  }

  def docker = new Docker(this)
  docker.run(config)
}
