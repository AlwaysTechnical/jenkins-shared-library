package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerPushSpec extends Specification {

    def "dockerPush delegates to Docker.push()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerPush.groovy', [image: 'myapp', registry: 'registry.example.com'])

        then:
        noExceptionThrown()
        mock.shCalls[0].script == 'docker push registry.example.com/myapp:latest'
    }

    def "dockerPush throws error when image is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerPush.groovy', [:])

        when:
        dockerPush([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"image" parameter is required')
    }

    def "dockerPush throws error when registry is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerPush.groovy', [image: 'myapp'])

        when:
        dockerPush(image: 'myapp')

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"registry" parameter is required')
    }
}