package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerLoginEcrSpec extends Specification {

    def "dockerLoginEcr delegates to Docker.loginEcr()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerLoginEcr.groovy', [region: 'us-east-1'])

        then:
        noExceptionThrown()
        mock.shCalls[0].script.contains('aws ecr get-login-password')
    }

    def "dockerLoginEcr throws error when region is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerLoginEcr.groovy', [:])

        when:
        dockerLoginEcr([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"region" parameter is required')
    }
}
