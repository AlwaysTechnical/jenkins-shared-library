package org.devops

class MockScript {

    Map env = [:]
    String BUILD_NUMBER = '1'
    String JOB_NAME = 'test-job'
    String BUILD_URL = 'http://jenkins/job/test/1/'
    String GIT_BRANCH = 'main'
    String GIT_PREVIOUS_SUCCESSFUL_COMMIT = ''
    def currentBuild = [result: 'SUCCESS']

    List<String> echoMessages = []
    List<Map> shCalls = []
    List<Map> slackSendCalls = []
    List<Map> mailCalls = []
    List<String> junitPatterns = []
    List<Map> coberturaCalls = []
    List<Map> checkoutCalls = []
    List<Map> withCredentialsCalls = []
    List<Map> sleepCalls = []
    List<String> fileExistsCalls = []
    List<String> errorMessages = []
    List<Map> timeoutCalls = []
    List<Map> inputCalls = []

    MockScript() {
        env = [
            'BUILD_NUMBER'              : BUILD_NUMBER,
            'JOB_NAME'                  : JOB_NAME,
            'BUILD_URL'                 : BUILD_URL,
            'GIT_BRANCH'                : GIT_BRANCH,
            'GIT_PREVIOUS_SUCCESSFUL_COMMIT': GIT_PREVIOUS_SUCCESSFUL_COMMIT,
        ]
    }

    void error(String message) {
        errorMessages << message
        throw new RuntimeException(message)
    }

    Object sh(String script) {
        return sh([script: script])
    }

    Object sh(Map config) {
        shCalls << config
        if (config.returnStdout) {
            return config.returnValue ?: ''
        }
        return null
    }

    void echo(String message) {
        echoMessages << message
    }

    void slackSend(Map config) {
        slackSendCalls << config
    }

    void mail(Map config) {
        mailCalls << config
    }

    void junit(String pattern) {
        junitPatterns << pattern
    }

    void junit(Map config) {
        junitPatterns << config.testResults
    }

    void cobertura(Map config) {
        coberturaCalls << config
    }

    void checkout(Map config) {
        checkoutCalls << config
    }

    void withCredentials(List bindings, Closure body) {
        withCredentialsCalls << [bindings: bindings, body: body]
        bindings.each { binding ->
            if (binding.'$class' == 'UsernamePasswordMultiBinding') {
                env[binding.usernameVariable] = 'test-user'
                env[binding.passwordVariable] = 'test-pass'
            } else if (binding.'$class' == 'AmazonWebServicesCredentialsBinding') {
                env[binding.accessKeyVariable] = 'test-access-key'
                env[binding.secretKeyVariable] = 'test-secret-key'
            } else if (binding.'$class' == 'GoogleServiceAccountKey') {
                env[binding.keyFileVariable] = '/tmp/gcloud-key.json'
            }
        }
        body.call()
    }

    void sleep(Map config) {
        sleepCalls << config
    }

    boolean fileExists(String path) {
        fileExistsCalls << path
        return false
    }

    void timeout(Map config, Closure body) {
        timeoutCalls << config
        body.call()
    }

    void input(Map config = [:]) {
        inputCalls << config
    }
}
