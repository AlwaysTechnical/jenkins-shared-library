package org.devops

/**
 * Centralised configuration manager.
 *
 * Provides sensible defaults for common pipeline settings (branch name,
 * notification channels, build tool, etc.) while allowing per-job
 * overrides supplied by the consuming Jenkinsfile.
 *
 * Configuration precedence (highest wins):
 *   1. Explicit call-time override
 *   2. Job-level config passed to {@code Config.withDefaults()}
 *   3. Library defaults defined in DEFAULTS
 */
class Config implements Serializable {
  private final Map values

  Config(Map values = [:]) {
    this.values = new HashMap<String, Object>(Config.DEFAULTS)
    if (values) {
      this.values.putAll(values)
    }
  }

  static final Map<String, Object> DEFAULTS = [
    branch             : 'main',
    buildTool          : 'maven',
    failOnError        : true,
    failOnWarning      : false,
    notifyOnFailure    : true,
    notifyOnSuccess    : false,
    slackChannel       : '#builds',
    slackServer        : null,
    emailRecipients    : '',
    gitCredentialsId   : '',
    timeoutMinutes     : 30,
    retryAttempts      : 1,
    retryDelaySeconds  : 0,
    stashIncludes      : '**/*.java, **/*.groovy, **/pom.xml, **/build.gradle',
    stashExcludes      : 'target/, build/, .git/',
    cleanWorkspace     : false,
  ]

  /**
   * Return the value for a key, falling back to {@code defaultValue}
   * when neither the override nor a library default is present.
   */
  def get(String key, def defaultValue = null) {
    if (values.containsKey(key) && values.get(key) != null) {
      return values[key]
    }
    if (Config.DEFAULTS.containsKey(key) && Config.DEFAULTS.get(key) != null) {
      return Config.DEFAULTS[key]
    }
    return defaultValue
  }

  /**
   * Return a typed value coerced to {@code type}.
   */
  <T> T getAs(String key, Class<T> type, T defaultValue = null) {
    def val = get(key, defaultValue)
    if (val == null) {
      return defaultValue
    }
    return val.asType(type)
  }

  @Override
  String toString() {
    return "Config${values}"
  }
}
