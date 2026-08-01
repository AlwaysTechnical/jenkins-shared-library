package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerCleanSpec extends Specification {

    def "dockerClean delegates to Docker.clean()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerClean.groovy', [:])

        then:
        noExceptionThrown()
        mock.shCalls.any { it.script == 'docker container prune -f' }
    }
}