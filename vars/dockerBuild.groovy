import org.devops.Docker

/**
 * Build a Docker image with optional tags, build args, and registry login.
 *
 * Usage:
 *   dockerBuild image: 'myapp', tags: ['1.0.0', 'latest']
 *   dockerBuild image: 'myapp', registry: 'registry.example.com', registryCredentialsId: 'docker-cred'
 *   dockerBuild image: 'myapp', buildArgs: [JAVA_VERSION: '17'], noCache: true
 *
 * @param config  map with:
 *   - image             : image name (required)
 *   - tags              : list of tags (default ['latest'])
 *   - buildArgs         : map of build arguments
 *   - registry          : registry URL (optional)
 *   - registryCredentialsId : credentials for registry login (optional)
 *   - dockerfile        : path to Dockerfile (default 'Dockerfile')
 *   - buildContext      : build context path (default '.')
 *   - noCache           : use --no-cache (default false)
 *   - pull              : use --pull (default false)
 *   - retryAttempts     : retry count (default 1)
 *   - retryDelaySeconds : retry delay in seconds (default 0)
 *   - failOnError       : fail on build error (default true)
 */
def call(Map config = [:]) {
  def image = config.image
  if (!image) {
    error('dockerBuild: "image" parameter is required')
  }

  def docker = new Docker(this)
  docker.build(config)
}
