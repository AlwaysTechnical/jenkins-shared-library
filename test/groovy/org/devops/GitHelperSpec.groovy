package org.devops

import spock.lang.Specification

class GitHelperSpec extends Specification {

    def "checkout() uses default branch when no branchSpec provided"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.checkout('https://github.com/org/repo.git')

        then:
        mock.checkoutCalls.size() == 1
        mock.checkoutCalls[0].branches == [[name: 'main']]
        mock.checkoutCalls[0].userRemoteConfigs == [[url: 'https://github.com/org/repo.git', credentialsId: '']]
    }

    def "checkout() uses provided branchSpec"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.checkout('https://github.com/org/repo.git', '', 'feature/my-branch')

        then:
        mock.checkoutCalls[0].branches == [[name: 'feature/my-branch']]
    }

    def "checkout() uses provided credentialsId"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.checkout('https://github.com/org/repo.git', 'git-cred')

        then:
        mock.checkoutCalls[0].userRemoteConfigs[0].credentialsId == 'git-cred'
    }

    def "checkout() generates changelog by default"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.checkout('https://github.com/org/repo.git')

        then:
        mock.checkoutCalls.size() == 1
    }

    def "checkout() skips changelog when generateChangelog is false"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.checkout('https://github.com/org/repo.git', '', null, false)

        then:
        mock.checkoutCalls.size() == 1
    }

    def "checkout() throws on error and logs message"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)
        mock.checkoutCalls << null

        when:
        helper.checkout('https://github.com/org/repo.git')

        then:
        noExceptionThrown()
    }

    def "tag() creates annotated tag"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.tag('v1.0.0', 'release')

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script == "git tag -a v1.0.0 -m 'release'"
    }

    def "tag() pushes to origin when credentialsId is provided"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.tag('v1.0.0', 'release', 'git-cred')

        then:
        mock.shCalls.size() == 2
        mock.shCalls[0].script == "git tag -a v1.0.0 -m 'release'"
        mock.shCalls[1].script == 'git push origin v1.0.0'
        mock.withCredentialsCalls.size() == 1
    }

    def "tag() does not push when credentialsId is empty"() {
        given:
        def mock = new MockScript()
        def helper = new GitHelper(mock)

        when:
        helper.tag('v1.0.0')

        then:
        mock.shCalls.size() == 1
        mock.withCredentialsCalls.isEmpty()
    }

    def "shortCommit() returns git rev-parse output"() {
        given:
        def mock = new MockScript()
        mock.env['GIT_COMMIT'] = 'abc123def456'
        def helper = new GitHelper(mock)

        when:
        def result = helper.shortCommit()

        then:
        result == ''
        mock.shCalls.size() == 1
        mock.shCalls[0].script == 'git rev-parse --short HEAD'
        mock.shCalls[0].returnStdout == true
    }
}
