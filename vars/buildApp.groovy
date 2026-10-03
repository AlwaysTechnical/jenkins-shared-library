import org.devops.BuildHelper
import org.devops.Config

/**
 * Build the project using the configured build tool (Maven or Gradle).
 *
 * Usage:
 *   buildApp tool: 'maven', goals: 'clean install'
 *   buildApp tool: 'gradle', goals: 'assemble', extraArgs: ['--parallel']
 *
 * @param config  map with:
 *   - tool             : 'maven' (default) or 'gradle'
 *   - goals            : build goals/phases
 *   - extraArgs        : list of extra CLI args
 *   - retryAttempts    : retries (default 1)
 *   - retryDelaySeconds: retry delay (default 0)
 *   - failOnError      : fail build on errors (default true)
 */
def call(Map config = [:]) {
  def cfg = new Config([
    buildTool         : config.tool ?: Config.DEFAULTS.buildTool,
    retryAttempts     : config.retryAttempts,
    retryDelaySeconds : config.retryDelaySeconds,
    failOnError       : config.failOnError,
    extraTestArgs     : config.extraTestArgs,
  ])

  def helper = new BuildHelper(this)
  helper.build(cfg, config.goals, config.extraArgs ?: [])
}
