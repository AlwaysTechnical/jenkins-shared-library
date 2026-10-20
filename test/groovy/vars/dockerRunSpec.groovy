package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerRunSpec extends Specification {

    def "dockerRun delegates to Docker.run()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerRun.groovy', [image: 'myapp'])

        then:
        noExceptionThrown()
        mock.shCalls[0].script.contains('docker run --rm myapp')
    }

    def "dockerRun throws error when image is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerRun.groovy', [:])

        when:
        dockerRun([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"image" parameter is required')
    }
}
