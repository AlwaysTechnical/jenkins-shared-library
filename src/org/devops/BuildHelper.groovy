package org.devops

/**
 * Build helper that abstracts over the build tooling
 * (Maven or Gradle) so consuming pipelines don't hard-code commands.
 */
class BuildHelper implements Serializable {
  private final Script context

  BuildHelper(Script context) {
    this.context = context
  }

  /**
   * Execute the build using the configured tool.
   *
   * @param config  Config with buildTool, retryAttempts, retryDelaySeconds
   * @param goals   Goals/phases to run (default 'clean package' or 'clean build')
   * @param extraArgs  Extra CLI arguments
   */
  void build(Config config, String goals = null, List<String> extraArgs = []) {
    def tool = config.get('buildTool', 'maven')
    def toolGoals = goals ?: (tool == 'gradle' ? 'clean build' : 'clean package')
    def attempts = config.getAs('retryAttempts', Integer, 1)
    def delay = (config.getAs('retryDelaySeconds', Integer, 0) * 1000L) as long
    def args = extraArgs.join(' ')

    def utils = new Utils(context)

    utils.retry(attempts, delay) {
      if (tool == 'gradle') {
        context.sh("./gradlew ${toolGoals} ${args} --no-daemon".trim())
      } else {
        def mvnOpts = '-B -ntp'
        if (config.getAs('failOnError', Boolean, true)) {
          mvnOpts += ' -Dmaven.test.skip=false'
        } else {
          mvnOpts += ' -Dmaven.test.skip=true'
        }
        context.sh("mvn ${mvnOpts} ${toolGoals} ${args}".trim())
      }
    }
  }

  /**
   * Run tests and publish JUnit reports + coverage if available.
   */
  void test(Config config, String goals = null) {
    def tool = config.get('buildTool', 'maven')
    def toolGoals = goals ?: (tool == 'gradle' ? 'test' : 'test')
    def args = config.get('extraTestArgs', '')

    context.sh(
      (tool == 'gradle' ? "./gradlew ${toolGoals} ${args}" : "mvn ${args} ${toolGoals}").trim(),
    )

    publishTestResults(config)
    publishCoverage(config)
  }

  private void publishTestResults(Config config) {
    def patterns = []
    def tool = config.get('buildTool', 'maven')
    if (tool == 'gradle') {
      patterns = ['build/test-results/test/*.xml', 'build/test-results/integrationTest/*.xml']
    } else {
      patterns = ['target/surefire-reports/*.xml', 'target/failsafe-reports/*.xml']
    }
    def allowed = patterns.find { p -> context.fileExists(p) }
    if (allowed) {
      context.junit allowed
    } else {
      context.echo('[devops-build] No JUnit result files found; skipping test report.')
    }
  }

  private void publishCoverage(Config config) {
    def tool = config.get('buildTool', 'maven')
    def reportDir
    def reportFiles
    if (tool == 'gradle') {
      reportDir = 'build/reports/jacoco/test'
      reportFiles = 'index.html'
    } else {
      reportDir = 'target/site/jacoco'
      reportFiles = 'index.html'
    }
    if (context.fileExists("${reportDir}/${reportFiles}")) {
      context.cobertura(
        autoUpdateHealth: false,
        autoUpdateStability: false,
        coberturaReportFile: "${reportDir}/${reportFiles}",
        conditionalCoverageTargets: '70, 80, 90',
        failNoReports: false,
        failUnstableReport: false,
        failUnstableReports: false,
        maxNumberOfBuilds: 0,
        onlyStable: false,
        onlyStableStarted: false,
        sourceEncoding: 'UTF_8',
        zoomCoverageChart: false,
      )
    }
  }
}
