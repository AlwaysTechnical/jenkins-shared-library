package org.devops

import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyObjectSupport

class VarsTestHelper {

    static MockScript loadVars(String varsFilePath, Map args = [:]) {
        def mockScript = new MockScript()
        def classLoader = new GroovyClassLoader()
        classLoader.addClasspath('src')
        def scriptClass = classLoader.parseClass(new File(varsFilePath))
        def script = scriptClass.newInstance()

        mockScript.metaClass.methods.each { method ->
            if (method.name != 'getClass' && method.name != 'getMetaClass' && !method.name.startsWith('$')) {
                script.metaClass."$method.name" = { Object... methodArgs ->
                    mockScript."$method.name"(*methodArgs)
                }
            }
        }

        script.call(args)
        return mockScript
    }
}
