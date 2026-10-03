package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerLoginGcrSpec extends Specification {

    def "dockerLoginGcr delegates to Docker.loginGcr()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerLoginGcr.groovy', [:])

        then:
        noExceptionThrown()
        mock.shCalls[0].script == 'gcloud auth configure-docker gcr.io --quiet'
    }
}
