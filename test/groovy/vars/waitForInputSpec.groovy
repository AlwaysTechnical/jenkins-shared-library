package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class waitForInputSpec extends Specification {

    def "waitForInput delegates to timeout and input steps"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/waitForInput.groovy', [message: 'Approve deploy?', timeoutMinutes: 30])

        then:
        noExceptionThrown()
        mock.timeoutCalls.size() == 1
        mock.timeoutCalls[0].time == 30
        mock.timeoutCalls[0].unit == 'MINUTES'
        mock.inputCalls.size() == 1
        mock.inputCalls[0].message == 'Approve deploy?'
    }

    def "waitForInput uses default message when none provided"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/waitForInput.groovy', [timeoutMinutes: 60])

        then:
        mock.inputCalls[0].message == 'Please approve to continue'
    }
}
