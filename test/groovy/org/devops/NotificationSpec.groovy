package org.devops

import spock.lang.Specification

class NotificationSpec extends Specification {

    def "send() does nothing when notifyOnFailure is false and status is FAILURE"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: false, notifyOnSuccess: false])
        def notifier = new Notification(mock)

        when:
        notifier.send('FAILURE', 'build failed', config)

        then:
        mock.slackSendCalls.isEmpty()
        mock.mailCalls.isEmpty()
    }

    def "send() sends slack and email on FAILURE when notifyOnFailure is true"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: true, slackChannel: '#alerts', emailRecipients: 'team@example.com'])
        def notifier = new Notification(mock)

        when:
        notifier.send('FAILURE', 'build failed', config)

        then:
        mock.slackSendCalls.size() == 1
        mock.slackSendCalls[0].channel == '#alerts'
        mock.slackSendCalls[0].color == 'danger'
        mock.slackSendCalls[0].message.contains('FAILURE')
        mock.slackSendCalls[0].message.contains('build failed')
        mock.mailCalls.size() == 1
        mock.mailCalls[0].to == 'team@example.com'
        mock.mailCalls[0].subject.contains('FAILURE')
    }

    def "send() sends slack and email on SUCCESS when notifyOnSuccess is true"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnSuccess: true, slackChannel: '#builds', emailRecipients: 'team@example.com'])
        def notifier = new Notification(mock)

        when:
        notifier.send('SUCCESS', 'build succeeded', config)

        then:
        mock.slackSendCalls.size() == 1
        mock.slackSendCalls[0].color == 'good'
        mock.mailCalls.size() == 1
    }

    def "send() sends notifications for UNSTABLE status regardless of flags"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: false, notifyOnSuccess: false])
        def notifier = new Notification(mock)

        when:
        notifier.send('UNSTABLE', 'tests unstable', config)

        then:
        mock.slackSendCalls.size() == 1
        mock.slackSendCalls[0].color == 'warning'
        mock.mailCalls.size() == 1
    }

    def "send() sends notifications for ABORTED status regardless of flags"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: false, notifyOnSuccess: false])
        def notifier = new Notification(mock)

        when:
        notifier.send('ABORTED', 'build aborted', config)

        then:
        mock.slackSendCalls.size() == 1
        mock.slackSendCalls[0].color == 'warning'
    }

    def "send() skips slack when slackChannel is not set"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: true, slackChannel: null, emailRecipients: 'team@example.com'])
        def notifier = new Notification(mock)

        when:
        notifier.send('FAILURE', 'build failed', config)

        then:
        mock.slackSendCalls.isEmpty()
        mock.mailCalls.size() == 1
    }

    def "send() skips email when emailRecipients is not set"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: true, slackChannel: '#alerts', emailRecipients: ''])
        def notifier = new Notification(mock)

        when:
        notifier.send('FAILURE', 'build failed', config)

        then:
        mock.slackSendCalls.size() == 1
        mock.mailCalls.isEmpty()
    }

    def "colorForStatus() returns correct colors"() {
        given:
        def mock = new MockScript()
        def notifier = new Notification(mock)

        expect:
        notifier.colorForStatus('SUCCESS') == 'good'
        notifier.colorForStatus('FAILURE') == 'danger'
        notifier.colorForStatus('UNSTABLE') == 'warning'
        notifier.colorForStatus('ABORTED') == 'warning'
        notifier.colorForStatus('UNKNOWN') == 'good'
    }

    def "send() includes job name and build number in message"() {
        given:
        def mock = new MockScript()
        def config = new Config([notifyOnFailure: true, slackChannel: '#alerts', emailRecipients: 'team@example.com'])
        def notifier = new Notification(mock)

        when:
        notifier.send('FAILURE', 'build failed', config)

        then:
        mock.slackSendCalls[0].message.contains('test-job')
        mock.slackSendCalls[0].message.contains('1')
    }
}