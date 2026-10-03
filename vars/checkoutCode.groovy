import org.devops.Config
import org.devops.GitHelper
import org.devops.Utils

/**
 * Checkout source code with retry support and optional changelog.
 *
 * Usage:
 *   checkoutCode url: 'https://github.com/org/repo.git', credentialsId: 'git-creds'
 *   checkoutCode url: '...', branch: 'develop', changelog: true
 *
 * @param config  map with:
 *   - url              : repository URL
 *   - credentialsId    : Jenkins credentials ID (optional)
 *   - branch           : branch/tag/ref to checkout (default 'main')
 *   - changelog        : generate changelog (default true)
 *   - retryAttempts    : number of retry attempts (default 1)
 *   - retryDelaySeconds: delay between retries (default 0)
 */
def call(Map config = [:]) {
  def cfg = new Config([
    gitCredentialsId  : config.credentialsId,
    branch            : config.branch,
    retryAttempts     : config.retryAttempts,
    retryDelaySeconds : config.retryDelaySeconds,
  ])

  def url = config.url
  if (!url) {
    error('checkoutCode: "url" parameter is required')
  }

  def helper = new GitHelper(this)
  def utils = new Utils(this)

  utils.retry(cfg.getAs('retryAttempts', Integer, 1) as int,
              (cfg.getAs('retryDelaySeconds', Integer, 0) * 1000L) as long) {
    helper.checkout(url, cfg.get('gitCredentialsId', ''), cfg.get('branch'), config.changelog != false)
  }
}
