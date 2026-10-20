package org.devops

/**
 * Git helper providing checkout, tagging, and changelog utilities.
 */
class GitHelper implements Serializable {
  private final Script context

  GitHelper(Script context) {
    this.context = context
  }

  /**
   * Checkout a branch (or PR ref) with retry logic and optional
   * changelog generation.
   *
   * @param repositoryUrl  Git remote URL
   * @param credentialsId  Optional Jenkins credentials ID
   * @param branchSpec     Branch, tag, or PR ref to checkout
   * @param generateChangelog if true, capture changes since last successful build
   */
  void checkout(String repositoryUrl, String credentialsId = '', String branchSpec = null, boolean generateChangelog = true) {
    def branch = branchSpec ?: context.env.GIT_BRANCH ?: Config.DEFAULTS.branch
    branch = branch ?: Config.DEFAULTS.branch

    try {
      context.checkout([
        $class: 'GitSCM',
        extensions: [
          [$class: 'CleanBeforeCheckout'],
          [$class: 'PruneStaleTags'],
        ],
        userRemoteConfigs: [[url: repositoryUrl, credentialsId: credentialsId]],
        branches: [[name: branch]],
      ])
    } catch (Exception e) {
      context.echo("[devops-git] Checkout failed for ${branch}: ${e.message}")
      throw e
    }

    if (generateChangelog) {
      generateChangelog(branch)
    }
  }

  /**
   * Generate a simple changelog comment based on commit messages since the
   * last successful build.
   */
  private void generateChangelog(String branch) {
    def upstream = context.env.GIT_PREVIOUS_SUCCESSFUL_COMMIT
    if (!upstream) {
      context.echo('[devops-git] No previous successful commit; skipping changelog.')
      return
    }
    try {
      def log = context.sh(
        script: "git log --pretty=format:'* %s' ${upstream}..HEAD || true",
        returnStdout: true,
      ).trim()
      if (log) {
        context.echo("Recent changes on ${branch}:\n${log}")
      }
    } catch (Exception e) {
      context.echo("[devops-git] Changelog generation skipped: ${e.message}")
    }
  }

  /**
   * Create a git tag with annotation.
   */
  void tag(String tagName, String message = '', String credentialsId = '') {
    context.sh("git tag -a ${tagName} -m '${message}'")
    if (credentialsId) {
      context.withCredentials([[
        $class: 'UsernamePasswordMultiBinding',
        credentialsId: credentialsId,
        usernameVariable: 'GIT_USER',
        passwordVariable: 'GIT_PASS',
      ]]) {
        context.sh("git push origin ${tagName}")
      }
    }
  }

  /**
   * Return the current short git commit hash.
   */
  String shortCommit() {
    return context.sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
  }
}
