import org.devops.Docker

/**
 * Tag a Docker image with a new tag.
 *
 * Usage:
 *   dockerTag image: 'myapp', tag: '1.0.0'
 *   dockerTag image: 'myapp', tag: '1.0.0', registry: 'registry.example.com'
 *
 * @param config  map with:
 *   - image   : source image name (required)
 *   - tag     : new tag (required)
 *   - registry : registry URL (optional)
 */
def call(Map config = [:]) {
  def image = config.image
  if (!image) {
    error('dockerTag: "image" parameter is required')
  }

  def tag = config.tag
  if (!tag) {
    error('dockerTag: "tag" parameter is required')
  }

  def docker = new Docker(this)
  docker.tag(config)
}