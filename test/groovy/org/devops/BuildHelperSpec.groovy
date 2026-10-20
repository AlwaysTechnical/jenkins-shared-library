package org.devops

import spock.lang.Specification

class BuildHelperSpec extends Specification {

    def "build() runs maven with clean package by default"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.build(config)

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script == 'mvn -B -ntp -Dmaven.test.skip=false clean package'
    }

    def "build() runs gradle with clean build"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'gradle'])
        def helper = new BuildHelper(mock)

        when:
        helper.build(config)

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script == './gradlew clean build --no-daemon'
    }

    def "build() passes extra args to maven"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.build(config, null, ['-DskipTests', '-Pprod'])

        then:
        mock.shCalls[0].script == 'mvn -B -ntp -Dmaven.test.skip=false clean package -DskipTests -Pprod'
    }

    def "build() skips tests when failOnError is false"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven', failOnError: false])
        def helper = new BuildHelper(mock)

        when:
        helper.build(config)

        then:
        mock.shCalls[0].script == 'mvn -B -ntp -Dmaven.test.skip=true clean package'
    }

    def "build() runs gradle with custom goals"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'gradle'])
        def helper = new BuildHelper(mock)

        when:
        helper.build(config, 'check')

        then:
        mock.shCalls[0].script == './gradlew check --no-daemon'
    }

    def "build() retries on failure"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven', retryAttempts: 3, retryDelaySeconds: 5])
        def helper = new BuildHelper(mock)
        def attempts = 0

        when:
        helper.build(config)

        then:
        mock.shCalls.size() == 1
        mock.sleepCalls.size() == 0
    }

    def "test() runs maven test"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.shCalls[0].script == 'mvn  test'
    }

    def "test() runs gradle test"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'gradle'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.shCalls[0].script == './gradlew test'
    }

    def "test() passes extraTestArgs"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven', extraTestArgs: '-Dtest=FooTest'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.shCalls[0].script == 'mvn -Dtest=FooTest test'
    }

    def "test() publishes junit results when surefire reports exist"() {
        given:
        def mock = new MockScript()
        mock.fileExistsCalls << 'target/surefire-reports/TEST-com.example.FooTest.xml'
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.junitPatterns.size() == 1
        mock.junitPatterns[0] == 'target/surefire-reports/*.xml'
    }

    def "test() skips junit when no result files found"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.junitPatterns.isEmpty()
        mock.echoMessages.any { it.contains('No JUnit result files found') }
    }

    def "test() publishes cobertura when jacoco report exists for maven"() {
        given:
        def mock = new MockScript()
        mock.fileExistsCalls << 'target/site/jacoco/index.html'
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.coberturaCalls.size() == 1
        mock.coberturaCalls[0].coberturaReportFile == 'target/site/jacoco/index.html'
    }

    def "test() publishes cobertura when jacoco report exists for gradle"() {
        given:
        def mock = new MockScript()
        mock.fileExistsCalls << 'build/reports/jacoco/test/index.html'
        def config = new Config([buildTool: 'gradle'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.coberturaCalls.size() == 1
        mock.coberturaCalls[0].coberturaReportFile == 'build/reports/jacoco/test/index.html'
    }

    def "test() skips cobertura when no jacoco report found"() {
        given:
        def mock = new MockScript()
        def config = new Config([buildTool: 'maven'])
        def helper = new BuildHelper(mock)

        when:
        helper.test(config)

        then:
        mock.coberturaCalls.isEmpty()
    }
}
