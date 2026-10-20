package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class runTestsSpec extends Specification {

    def "runTests delegates to BuildHelper.test()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/runTests.groovy', [tool: 'maven', goals: 'test'])

        then:
        noExceptionThrown()
        mock.shCalls.size() >= 1
    }
}
