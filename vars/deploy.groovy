import org.devops.Config
import org.devops.GitHelper
import org.devops.Notification

/**
 * Generic deploy helper. Executes the provided deploy command/script,
 * tags git on success, and notifies on completion.
 *
 * Usage:
 *   deploy(
 *     command: './scripts/deploy.sh staging',
 *     version: '1.2.3',
 *     credentialsId: 'deploy-creds'
 *   )
 *
 * @param config  map with:
 *   - command        : shell command(s) to run for deployment
 *   - version        : version string used for git tag (optional)
 *   - tag            : whether to create a git tag (default true when version set)
 *   - credentialsId  : credentials for pushing tags (optional)
 *   - url            : git repo URL for tagging (optional)
 *   - slackChannel   : notification override
 *   - emailRecipients: notification override
 */
def call(Map config = [:]) {
  def cfg = new Config([
    gitCredentialsId  : config.credentialsId,
    slackChannel      : config.slackChannel,
    emailRecipients   : config.emailRecipients,
  ])

  def command = config.command
  if (!command) {
    error('deploy: "command" parameter is required')
  }

  def utils = new org.devops.Utils(this)
  def start = System.currentTimeMillis()

  try {
    utils.time('Deploy', {
      sh command
    } as Closure)

    if (config.version && config.tag != false) {
      def gitUrl = config.url
      if (gitUrl) {
        new GitHelper(this).tag(
          config.version,
          "Release ${config.version}",
          cfg.get('gitCredentialsId', ''),
        )
      }
    }

    def elapsed = System.currentTimeMillis() - start
    new Notification(this).send('SUCCESS', "Deploy succeeded in ${elapsed}ms", cfg)
  } catch (Exception e) {
    new Notification(this).send('FAILURE', "Deploy failed: ${e.message}", cfg)
    throw e
  }
}
