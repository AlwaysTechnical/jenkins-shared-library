package org.devops

import spock.lang.Specification

class DockerSpec extends Specification {

    def "build() requires image parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"image" parameter is required')
    }

    def "build() uses default tag latest"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp')

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script.contains('-t myapp:latest')
    }

    def "build() applies multiple tags"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp', tags: ['v1', 'latest'])

        then:
        mock.shCalls[0].script.contains('-t myapp:v1')
        mock.shCalls[0].script.contains('-t myapp:latest')
    }

    def "build() passes build args"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp', buildArgs: [VERSION: '1.0', ENV: 'prod'])

        then:
        mock.shCalls[0].script.contains('--build-arg VERSION=1.0')
        mock.shCalls[0].script.contains('--build-arg ENV=prod')
    }

    def "build() uses custom dockerfile and build context"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp', dockerfile: 'Dockerfile.prod', buildContext: './dist')

        then:
        mock.shCalls[0].script.contains('-f Dockerfile.prod')
        mock.shCalls[0].script.contains('./dist')
    }

    def "build() includes --no-cache when noCache is true"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp', noCache: true)

        then:
        mock.shCalls[0].script.contains('--no-cache')
    }

    def "build() includes --pull when pull is true"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp', pull: true)

        then:
        mock.shCalls[0].script.contains('--pull')
    }

    def "build() logs in and out of registry when credentials provided"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.build(image: 'myapp', registry: 'registry.example.com', registryCredentialsId: 'docker-cred')

        then:
        mock.shCalls.size() >= 1
        mock.withCredentialsCalls.size() >= 1
    }

    def "push() requires image parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.push([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"image" parameter is required')
    }

    def "push() requires registry parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.push(image: 'myapp')

        then:
        def e = thrown(Exception)
        e.message.contains('"registry" parameter is required')
    }

    def "push() pushes to registry with tag"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.push(image: 'myapp', registry: 'registry.example.com', tag: 'v1')

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script == 'docker push registry.example.com/myapp:v1'
    }

    def "login() requires registry parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.login([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"registry" parameter is required')
    }

    def "login() uses credentialsId when provided"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.login(registry: 'registry.example.com', credentialsId: 'docker-cred')

        then:
        mock.withCredentialsCalls.size() == 1
        mock.shCalls.size() == 1
    }

    def "login() uses username and password when provided"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.login(registry: 'registry.example.com', username: 'user', password: 'pass')

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script.contains('docker login registry.example.com -u user -p pass')
    }

    def "login() falls back to anonymous login"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.login(registry: 'registry.example.com')

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script == 'docker login registry.example.com'
    }

    def "logout() uses default Docker Hub registry"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.logout()

        then:
        mock.shCalls.size() == 1
        mock.shCalls[0].script == 'docker logout https://index.docker.io/v1/'
    }

    def "logout() uses custom registry"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.logout(registry: 'registry.example.com')

        then:
        mock.shCalls[0].script == 'docker logout registry.example.com'
    }

    def "tag() requires image parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.tag([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"image" parameter is required')
    }

    def "tag() requires tag parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.tag(image: 'myapp')

        then:
        def e = thrown(Exception)
        e.message.contains('"tag" parameter is required')
    }

    def "tag() tags without registry"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.tag(image: 'myapp', tag: 'v1')

        then:
        mock.shCalls[0].script == 'docker tag myapp:latest myapp:v1'
    }

    def "tag() tags with registry"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.tag(image: 'myapp', tag: 'v1', registry: 'registry.example.com')

        then:
        mock.shCalls[0].script == 'docker tag registry.example.com/myapp:latest registry.example.com/myapp:v1'
    }

    def "run() requires image parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"image" parameter is required')
    }

    def "run() runs container with default options"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run(image: 'myapp')

        then:
        mock.shCalls[0].script.contains('docker run --rm myapp')
    }

    def "run() runs container in detached mode"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run(image: 'myapp', detached: true)

        then:
        mock.shCalls[0].script.contains('-d')
    }

    def "run() passes environment variables"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run(image: 'myapp', env: [KEY: 'value'])

        then:
        mock.shCalls[0].script.contains('-e KEY=value')
    }

    def "run() passes port mappings"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run(image: 'myapp', ports: ['8080:8080'])

        then:
        mock.shCalls[0].script.contains('-p 8080:8080')
    }

    def "run() passes volume mounts"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run(image: 'myapp', volumes: ['/host:/container'])

        then:
        mock.shCalls[0].script.contains('-v /host:/container')
    }

    def "run() does not remove container when rm is false"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.run(image: 'myapp', rm: false)

        then:
        !mock.shCalls[0].script.contains('--rm')
    }

    def "compose() requires command parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.compose([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"command" parameter is required')
    }

    def "compose() runs up command"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.compose(command: 'up')

        then:
        mock.shCalls[0].script == 'docker-compose -f docker-compose.yml up'
    }

    def "compose() uses custom compose file"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.compose(command: 'down', composeFile: 'docker-compose.prod.yml')

        then:
        mock.shCalls[0].script == 'docker-compose -f docker-compose.prod.yml down'
    }

    def "clean() runs container prune by default"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.clean()

        then:
        mock.shCalls.any { it.script == 'docker container prune -f' }
    }

    def "clean() prunes images with force flag when pruneAll is true"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.clean(pruneAll: true)

        then:
        mock.shCalls.any { it.script == 'docker image prune -af' }
    }

    def "clean() skips container prune when containers is false"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.clean(containers: false)

        then:
        !mock.shCalls.any { it.script == 'docker container prune -f' }
    }

    def "inspect() requires target parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.inspect([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"target" parameter is required')
    }

    def "inspect() returns raw output"() {
        given:
        def mock = new MockScript()
        mock.env['BUILD_NUMBER'] = '1'
        def docker = new Docker(mock)

        when:
        docker.inspect(target: 'myapp:latest')

        then:
        mock.shCalls[0].script == "docker inspect --format '{{json .}}' myapp:latest"
        mock.shCalls[0].returnStdout == true
    }

    def "inspect() uses custom format"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.inspect(target: 'myapp:latest', format: '{{.Id}}')

        then:
        mock.shCalls[0].script == "docker inspect --format '{{.Id}}' myapp:latest"
    }

    def "loginEcr() requires region parameter"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginEcr([:])

        then:
        def e = thrown(Exception)
        e.message.contains('"region" parameter is required')
    }

    def "loginEcr() derives registry from accountId"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginEcr(region: 'us-east-1', accountId: '123456789012')

        then:
        mock.shCalls[0].script.contains('123456789012.dkr.ecr.us-east-1.amazonaws.com')
    }

    def "loginEcr() uses provided registry when accountId is absent"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginEcr(region: 'us-east-1', registry: '123456789012.dkr.ecr.us-east-1.amazonaws.com')

        then:
        mock.shCalls[0].script.contains('123456789012.dkr.ecr.us-east-1.amazonaws.com')
    }

    def "loginEcr() uses credentialsId when provided"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginEcr(region: 'us-east-1', credentialsId: 'aws-cred')

        then:
        mock.withCredentialsCalls.size() == 1
    }

    def "loginGcr() uses default registry"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginGcr()

        then:
        mock.shCalls[0].script == 'gcloud auth configure-docker gcr.io --quiet'
    }

    def "loginGcr() uses custom registry"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginGcr(registry: 'us.gcr.io')

        then:
        mock.shCalls[0].script == 'gcloud auth configure-docker us.gcr.io --quiet'
    }

    def "loginGcr() uses credentialsId when provided"() {
        given:
        def mock = new MockScript()
        def docker = new Docker(mock)

        when:
        docker.loginGcr(credentialsId: 'gcr-cred')

        then:
        mock.withCredentialsCalls.size() == 1
    }
}
