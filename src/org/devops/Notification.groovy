package org.devops

/**
 * Notification helper supporting Slack and email backends.
 *
 * Usage (from a vars step):
 *   def notifier = new Notification(this)
 *   notifier.send(currentBuild.result ?: 'SUCCESS', 'Build finished')
 */
class Notification implements Serializable {
  private final Script context

  Notification(Script context) {
    this.context = context
  }

  /**
   * Send a notification to configured channels.
   *
   * @param status     'SUCCESS', 'FAILURE', 'UNSTABLE', 'ABORTED'
   * @param message    Human-readable message body
   * @param config     Config instance with recipient details
   */
  void send(String status, String message, Config config) {
    def color = colorForStatus(status)
    def fullMessage = "${status}: ${message}\n${context.env.JOB_NAME} #${context.env.BUILD_NUMBER}\n${context.env.BUILD_URL}"

    if (config.getAs('notifyOnFailure', Boolean, false) && status == 'FAILURE'
        || config.getAs('notifyOnSuccess', Boolean, false) && status == 'SUCCESS'
        || status == 'UNSTABLE' || status == 'ABORTED') {
      sendSlack(fullMessage, color, config)
      sendEmail(fullMessage, status, config)
    }
  }

  private static String colorForStatus(String status) {
    switch (status) {
      case 'SUCCESS':
        return 'good'
      case 'FAILURE':
        return 'danger'
      case 'UNSTABLE':
        return 'warning'
      case 'ABORTED':
        return 'warning'
      default:
        return 'good'
    }
  }

  private void sendSlack(String message, String color, Config config) {
    def channel = config.get('slackChannel')
    if (!channel) {
      return
    }
    try {
      context.slackSend(
        channel: channel,
        color: color,
        message: message,
        failOnError: false,
      )
    } catch (Exception e) {
      context.echo("[devops-notification] Slack notification failed: ${e.message}")
    }
  }

  private void sendEmail(String message, String status, Config config) {
    def recipients = config.get('emailRecipients')
    if (!recipients) {
      return
    }
    try {
      context.mail(
        to: recipients,
        subject: "[${status}] ${context.env.JOB_NAME} #${context.env.BUILD_NUMBER}",
        body: message,
      )
    } catch (Exception e) {
      context.echo("[devops-notification] Email notification failed: ${e.message}")
    }
  }
}
