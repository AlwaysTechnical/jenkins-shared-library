package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class dockerBuildSpec extends Specification {

    def "dockerBuild delegates to Docker.build()"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/dockerBuild.groovy', [image: 'myapp', tags: ['latest']])

        then:
        noExceptionThrown()
        mock.shCalls.size() == 1
    }

    def "dockerBuild throws error when image is missing"() {
        given:
        VarsTestHelper.loadVars('vars/dockerBuild.groovy', [:])

        when:
        dockerBuild([:])

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"image" parameter is required')
    }
}
