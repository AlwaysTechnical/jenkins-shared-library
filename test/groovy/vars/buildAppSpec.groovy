package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class buildAppSpec extends Specification {

    def "buildApp delegates to BuildHelper.build() with maven"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/buildApp.groovy', [tool: 'maven', goals: 'clean package'])

        then:
        noExceptionThrown()
        mock.shCalls.size() == 1
        mock.shCalls[0].script.contains('mvn')
    }

    def "buildApp delegates to BuildHelper.build() with gradle"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/buildApp.groovy', [tool: 'gradle', goals: 'clean build'])

        then:
        noExceptionThrown()
        mock.shCalls.size() == 1
        mock.shCalls[0].script.contains('./gradlew')
    }
}
