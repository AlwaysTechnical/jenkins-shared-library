package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerLogoutSpec extends Specification {

    def "dockerLogout delegates to Docker.logout()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerLogout.groovy', [:])

        then:
        noExceptionThrown()
        mock.shCalls[0].script == 'docker logout https://index.docker.io/v1/'
    }
}
