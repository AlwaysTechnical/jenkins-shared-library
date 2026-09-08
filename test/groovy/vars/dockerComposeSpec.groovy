package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerComposeSpec extends Specification {

    def "dockerCompose delegates to Docker.compose()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerCompose.groovy', [command: 'up'])

        then:
        noExceptionThrown()
        mock.shCalls[0].script == 'docker-compose -f docker-compose.yml up'
    }

    def "dockerCompose throws error when command is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerCompose.groovy', [:])

        when:
        dockerCompose([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"command" parameter is required')
    }
}
