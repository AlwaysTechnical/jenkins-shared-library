package org.devops

import spock.lang.Specification

class UtilsSpec extends Specification {

    def "info() captures echo message with prefix"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)

        when:
        utils.info("hello world")

        then:
        mock.echoMessages == ["[devops-utils] hello world"]
    }

    def "warn() captures echo message with warning prefix"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)

        when:
        utils.warn("something looks wrong")

        then:
        mock.echoMessages == ["[devops-utils] WARNING: something looks wrong"]
    }

    def "error() captures echo message with error prefix"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)

        when:
        utils.error("something broke")

        then:
        mock.echoMessages == ["[devops-utils] ERROR: something broke"]
    }

    def "time() returns closure result and logs duration"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)

        when:
        def result = utils.time("myLabel") { 42 }

        then:
        result == 42
        mock.echoMessages.size() == 1
        mock.echoMessages[0].startsWith("[devops-utils] myLabel completed in ")
        mock.echoMessages[0].endsWith("ms")
    }

    def "time() rethrows exception from closure"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)

        when:
        utils.time("myLabel") { throw new RuntimeException("boom") }

        then:
        def e = thrown(RuntimeException)
        e.message == "boom"
    }

    def "retry() succeeds on first attempt"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)
        def attempts = 0

        when:
        def result = utils.retry(3, 0) {
            attempts++
            "success"
        }

        then:
        result == "success"
        attempts == 1
    }

    def "retry() retries on failure and succeeds on later attempt"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)
        def attempts = 0

        when:
        def result = utils.retry(3, 0) {
            attempts++
            if (attempts < 2) throw new RuntimeException("not yet")
            "success"
        }

        then:
        result == "success"
        attempts == 2
        mock.echoMessages.any { it.contains("Attempt 1 of 3 failed") }
    }

    def "retry() throws last error after exhausting attempts"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)

        when:
        utils.retry(3, 0) { throw new RuntimeException("always fails") }

        then:
        def e = thrown(RuntimeException)
        e.message == "always fails"
        mock.echoMessages.size() == 2
    }

    def "retry() with delay calls sleep between attempts"() {
        given:
        def mock = new MockScript()
        def utils = new Utils(mock)
        def attempts = 0

        when:
        utils.retry(2, 5000) {
            attempts++
            if (attempts < 2) throw new RuntimeException("not yet")
            "success"
        }

        then:
        mock.sleepCalls.size() == 1
        mock.sleepCalls[0].time == 5
        mock.sleepCalls[0].unit == 'SECONDS'
    }

    def "get() returns value from map"() {
        expect:
        Utils.get([key: "value"], "key") == "value"
    }

    def "get() returns default when key missing"() {
        expect:
        Utils.get([:], "key", "default") == "default"
    }

    def "get() returns null when key missing and no default"() {
        expect:
        Utils.get([:], "key") == null
    }

    def "get() handles null map gracefully"() {
        expect:
        Utils.get(null, "key", "default") == "default"
    }
}