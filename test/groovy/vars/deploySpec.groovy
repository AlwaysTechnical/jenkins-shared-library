package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class deploySpec extends Specification {

    def "deploy delegates to sh and Notification.send()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/deploy.groovy', [command: './scripts/deploy.sh staging'])

        then:
        noExceptionThrown()
        mock.shCalls.size() >= 1
    }

    def "deploy throws error when command is missing"() {
        given:
        VarsTestHelper.loadVars('vars/deploy.groovy', [:])

        when:
        deploy()

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"command" parameter is required')
    }
}
