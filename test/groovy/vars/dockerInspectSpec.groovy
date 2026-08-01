package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerInspectSpec extends Specification {

    def "dockerInspect delegates to Docker.inspect()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerInspect.groovy', [target: 'myapp:latest'])

        then:
        noExceptionThrown()
        mock.shCalls[0].returnStdout == true
    }

    def "dockerInspect throws error when target is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerInspect.groovy', [:])

        when:
        dockerInspect([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"target" parameter is required')
    }
}