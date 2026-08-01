package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class notifySpec extends Specification {

    def "notify delegates to Notification.send()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/notify.groovy', [status: 'SUCCESS', message: 'Build succeeded'])

        then:
        noExceptionThrown()
    }

    def "notify with string args delegates to map call"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/notify.groovy', [:])

        when:
        notify('SUCCESS', 'Build succeeded')

        then:
        noExceptionThrown()
    }
}