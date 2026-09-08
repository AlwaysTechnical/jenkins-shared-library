import org.devops.Config
import org.devops.Notification

/**
 * Send a notification (Slack and/or email) about the current build status.
 *
 * Usage:
 *   notify message: 'Build completed', status: 'SUCCESS'
 *   notify status: currentBuild.result ?: 'SUCCESS', message: 'Done'
 *
 * @param config  optional map overriding defaults:
 *   - status       : SUCCESS | FAILURE | UNSTABLE | ABORTED
 *   - message      : message body
 *   - slackChannel : override default Slack channel
 *   - emailRecipients : override default email recipients
 */
def call(Map config = [:]) {
  def status = config.status ?: (currentBuild?.result ?: 'SUCCESS')
  def message = config.message ?: "Build ${status}"

  def merged = new Config([
    slackChannel      : config.slackChannel,
    emailRecipients   : config.emailRecipients,
    notifyOnFailure   : config.notifyOnFailure != null ? config.notifyOnFailure : Config.DEFAULTS.notifyOnFailure,
    notifyOnSuccess   : config.notifyOnSuccess != null ? config.notifyOnSuccess : Config.DEFAULTS.notifyOnSuccess,
    slackServer       : config.slackServer,
  ])

  def notifier = new Notification(this)
  notifier.send(status, message, merged)
}

def call(String status, String message) {
  call([status: status, message: message])
}
