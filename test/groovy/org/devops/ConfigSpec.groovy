package org.devops

import spock.lang.Specification

class ConfigSpec extends Specification {

    def "defaults are set correctly"() {
        given:
        def config = new Config()

        then:
        config.get('branch') == 'main'
        config.get('buildTool') == 'maven'
        config.get('failOnError') == true
        config.get('failOnWarning') == false
        config.get('notifyOnFailure') == true
        config.get('notifyOnSuccess') == false
        config.get('slackChannel') == '#builds'
        config.get('slackServer') == null
        config.get('emailRecipients') == ''
        config.get('gitCredentialsId') == ''
        config.get('timeoutMinutes') == 30
        config.get('retryAttempts') == 1
        config.get('retryDelaySeconds') == 0
    }

    def "overrides take precedence over defaults"() {
        given:
        def config = new Config([branch: 'develop', buildTool: 'gradle'])

        then:
        config.get('branch') == 'develop'
        config.get('buildTool') == 'gradle'
        config.get('failOnError') == true
    }

    def "get() falls back to default when override is null"() {
        given:
        def config = new Config([slackChannel: null])

        then:
        config.get('slackChannel') == '#builds'
    }

    def "get() returns explicit defaultValue when key is not in map or defaults"() {
        given:
        def config = new Config()

        then:
        config.get('nonexistent', 'fallback') == 'fallback'
    }

    def "getAs() coerces value to requested type"() {
        given:
        def config = new Config([timeoutMinutes: 60])

        when:
        def result = config.getAs('timeoutMinutes', Integer, 30)

        then:
        result instanceof Integer
        result == 60
    }

    def "getAs() returns default when key is missing"() {
        given:
        def config = new Config()

        when:
        def result = config.getAs('nonexistent', String, 'default')

        then:
        result == 'default'
    }

    def "getAs() returns default when value is null"() {
        given:
        def config = new Config([slackChannel: null])

        when:
        def result = config.getAs('slackChannel', String, '#fallback')

        then:
        result == '#fallback'
    }

    def "toString() returns formatted string"() {
        given:
        def config = new Config([branch: 'test'])

        when:
        def str = config.toString()

        then:
        str.startsWith('Config')
        str.contains('branch')
    }
}