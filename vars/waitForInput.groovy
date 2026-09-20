import org.devops.Config

/**
 * Pause the pipeline for manual approval with an optional timeout.
 *
 * Usage:
 *   waitForInput message: 'Approve production deploy?', timeoutMinutes: 1440
 *   waitForInput message: 'Safe to proceed?'
 *
 * @param config  map with:
 *   - message        : prompt shown to the user
 *   - ok             : label for the approval button (default 'Approve')
 *   - submitter      : user/group allowed to approve (optional)
 *   - timeoutMinutes : max wait before failing (default 1440 = 24h)
 *   - abortOnTimeout : fail the build on timeout (default true)
 */
def call(Map config = [:]) {
  def cfg = new Config([
    timeoutMinutes: config.timeoutMinutes,
  ])
  def minutes = config.timeoutMinutes ?: cfg.getAs('timeoutMinutes', Integer, 1440) as int

  timeout(time: minutes, unit: 'MINUTES') {
    input(
      message: config.message ?: 'Please approve to continue',
      ok: config.ok ?: 'Approve',
      submitter: config.submitter,
      submitterParameter: config.submitterParameter,
      parameters: config.parameters ?: [],
    )
  }
}
