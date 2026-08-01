package vars

import org.devops.VarsTestHelper
import spock.lang.Specification

class checkoutCodeSpec extends Specification {

    def "checkoutCode delegates to GitHelper.checkout() with url"() {
        given:
        def mock = VarsTestHelper.loadVars('vars/checkoutCode.groovy', [url: 'https://github.com/org/repo.git'])

        then:
        noExceptionThrown()
        mock.checkoutCalls.size() == 1
        mock.checkoutCalls[0].userRemoteConfigs[0].url == 'https://github.com/org/repo.git'
    }

    def "checkoutCode throws error when url is missing"() {
        given:
        VarsTestHelper.loadVars('vars/checkoutCode.groovy', [:])

        when:
        checkoutCode()

        then:
        def e = thrown(RuntimeException)
        e.message.contains('"url" parameter is required')
    }
}
