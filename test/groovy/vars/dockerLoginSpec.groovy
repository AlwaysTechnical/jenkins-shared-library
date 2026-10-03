package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerLoginSpec extends Specification {

    def "dockerLogin delegates to Docker.login()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerLogin.groovy', [registry: 'registry.example.com'])

        then:
        noExceptionThrown()
        mock.shCalls[0].script == 'docker login registry.example.com'
    }

    def "dockerLogin throws error when registry is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerLogin.groovy', [:])

        when:
        dockerLogin([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"registry" parameter is required')
    }
}
