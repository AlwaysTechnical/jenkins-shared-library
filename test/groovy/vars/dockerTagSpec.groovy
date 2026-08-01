package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerTagSpec extends Specification {

    def "dockerTag delegates to Docker.tag()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerTag.groovy', [image: 'myapp', tag: 'v1'])

        then:
        noExceptionThrown()
        mock.shCalls[0].script == 'docker tag myapp:latest myapp:v1'
    }

    def "dockerTag throws error when image is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerTag.groovy', [:])

        when:
        dockerTag([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"image" parameter is required')
    }

    def "dockerTag throws error when tag is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerTag.groovy', [image: 'myapp'])

        when:
        dockerTag(image: 'myapp')

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"tag" parameter is required')
    }
}