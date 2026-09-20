package org.devops

/**
 * Core utility class providing shared helpers for logging, timing,
 * error handling, and retry logic across all pipeline steps.
 *
 * Implements Serializable so instances survive pipeline replays
 * when used inside a @Library context.
 */
class Utils implements Serializable {
  private final Script context

  Utils(Script context) {
    this.context = context
  }

  /**
   * Log an informational message prefixed with the library name.
   */
  void info(String message) {
    context.echo("[devops-utils] ${message}")
  }

  /**
   * Log a warning message.
   */
  void warn(String message) {
    context.echo("[devops-utils] WARNING: ${message}")
  }

  /**
   * Log an error message.
   */
  void error(String message) {
    context.echo("[devops-utils] ERROR: ${message}")
  }

  /**
   * Run a closure with timing instrumentation. Returns the closure's result.
   */
  <T> T time(String label, Closure<T> body) {
    def start = System.currentTimeMillis()
    try {
      return body()
    } finally {
      def elapsed = System.currentTimeMillis() - start
      info("${label} completed in ${elapsed}ms")
    }
  }

  /**
   * Retry a closure up to {@code maxAttempts} times with an optional
   * delay between attempts. Re-throws the last exception on failure.
   */
  <T> T retry(int maxAttempts, long delayMs = 0, Closure<T> body) {
    def lastError
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return body()
      } catch (Exception e) {
        lastError = e
        if (attempt < maxAttempts) {
          warn("Attempt ${attempt} of ${maxAttempts} failed: ${e.message}. Retrying...")
          if (delayMs > 0) {
            context.sleep(time: (long) (delayMs / 1000), unit: 'SECONDS')
          }
        }
      }
    }
    throw lastError
  }

  /**
   * Safely retrieve a nested map value with a default fallback.
   */
  static <T> T get(Map config, String key, T defaultValue = null) {
    return config?.get(key, defaultValue) as T
  }
}
