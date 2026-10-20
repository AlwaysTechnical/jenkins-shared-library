import org.devops.BuildHelper
import org.devops.Config

/**
 * Run tests, publish JUnit reports, and publish coverage when available.
 *
 * Usage:
 *   runTests tool: 'maven'
 *   runTests tool: 'gradle', goals: 'check'
 *
 * @param config  map with:
 *   - tool        : 'maven' (default) or 'gradle'
 *   - goals       : test goals
 *   - extraTestArgs : extra CLI args for the test phase
 */
def call(Map config = [:]) {
  def cfg = new Config([
    buildTool    : config.tool ?: Config.DEFAULTS.buildTool,
    extraTestArgs: config.extraTestArgs,
  ])

  def helper = new BuildHelper(this)
  helper.test(cfg, config.goals)
}
