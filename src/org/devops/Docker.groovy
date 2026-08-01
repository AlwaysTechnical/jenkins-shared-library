package org.devops

/**
 * Docker helper providing build, push, login, tag, run, compose,
 * clean, inspect, and registry-specific login operations.
 *
 * All methods delegate to Docker CLI commands via {@code sh} and
 * integrate with the library's retry, timing, and notification utilities.
 */
class Docker implements Serializable {
  private final Script context

  Docker(Script context) {
    this.context = context
  }

  /**
   * Build a Docker image with optional tags, build args, and registry login.
   *
   * @param config  map with:
   *   - image             : image name (required)
   *   - tags              : list of tags (default ['latest'])
   *   - buildArgs         : map of build arguments
   *   - registry          : registry URL (optional)
   *   - registryCredentialsId : credentials for registry login (optional)
   *   - dockerfile        : path to Dockerfile (default 'Dockerfile')
   *   - buildContext      : build context path (default '.')
   *   - noCache           : use --no-cache (default false)
   *   - pull              : use --pull (default false)
   *   - retryAttempts     : retry count (default 1)
   *   - retryDelaySeconds : retry delay in seconds (default 0)
   *   - failOnError       : fail on build error (default true)
   */
  void build(Map config = [:]) {
    def utils = new Utils(context)
    def image = config.image
    if (!image) {
      error('dockerBuild: "image" parameter is required')
    }

    def tags = config.tags ?: ['latest']
    def buildArgs = config.buildArgs ?: [:]
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def buildContext = config.buildContext ?: '.'
    def noCache = config.noCache ? '--no-cache' : ''
    def pull = config.pull ? '--pull' : ''
    def registry = config.registry
    def registryCreds = config.registryCredentialsId
    def attempts = config.retryAttempts ?: 1
    def delay = ((config.retryDelaySeconds ?: 0) * 1000L) as long

    def allTags = tags.collect { tag ->
      registry ? "${registry}/${image}:${tag}" : "${image}:${tag}"
    }

    utils.retry(attempts, delay) {
      if (registry && registryCreds) {
        login([registry: registry, credentialsId: registryCreds])
      }

      def argsList = buildArgs.collect { k, v -> "--build-arg ${k}=${v}" }.join(' ')
      def tagFlags = allTags.collect { t -> "-t ${t}" }.join(' ')

      context.sh(
        "docker build ${pull} ${noCache} ${argsList} ${tagFlags} -f ${dockerfile} ${buildContext}".trim(),
      )

      if (registry && registryCreds) {
        logout([registry: registry])
      }
    }
  }

  /**
   * Push a Docker image tag to a registry.
   *
   * @param config  map with:
   *   - image             : image name (required)
   *   - tag               : tag to push (default 'latest')
   *   - registry          : registry URL (required for push)
   *   - registryCredentialsId : credentials for registry login (optional)
   *   - retryAttempts     : retry count (default 1)
   *   - retryDelaySeconds : retry delay in seconds (default 0)
   */
  void push(Map config = [:]) {
    def utils = new Utils(context)
    def image = config.image
    if (!image) {
      error('dockerPush: "image" parameter is required')
    }
    def registry = config.registry
    if (!registry) {
      error('dockerPush: "registry" parameter is required')
    }

    def tag = config.tag ?: 'latest'
    def fullImage = "${registry}/${image}:${tag}"
    def registryCreds = config.registryCredentialsId
    def attempts = config.retryAttempts ?: 1
    def delay = ((config.retryDelaySeconds ?: 0) * 1000L) as long

    utils.retry(attempts, delay) {
      if (registryCreds) {
        login([registry: registry, credentialsId: registryCreds])
      }
      context.sh("docker push ${fullImage}")
      if (registryCreds) {
        logout([registry: registry])
      }
    }
  }

  /**
   * Login to a Docker registry.
   *
   * @param config  map with:
   *   - registry          : registry URL (required)
   *   - username          : registry username (optional, uses credentials if provided)
   *   - password          : registry password (optional, uses credentials if provided)
   *   - credentialsId     : Jenkins credentials ID for registry auth
   */
  void login(Map config = [:]) {
    def registry = config.registry
    if (!registry) {
      error('dockerLogin: "registry" parameter is required')
    }

    def username = config.username
    def password = config.password
    def credsId = config.credentialsId

    if (credsId) {
      context.withCredentials([[
        $class       : 'UsernamePasswordMultiBinding',
        credentialsId: credsId,
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS',
      ]]) {
        context.sh("docker login ${registry} -u \${DOCKER_USER} -p \${DOCKER_PASS}")
      }
    } else if (username && password) {
      context.sh("docker login ${registry} -u ${username} -p ${password}")
    } else {
      context.sh("docker login ${registry}")
    }
  }

  /**
   * Logout from a Docker registry.
   *
   * @param config  map with:
   *   - registry : registry URL (default Docker Hub)
   */
  void logout(Map config = [:]) {
    def registry = config.registry ?: 'https://index.docker.io/v1/'
    context.sh("docker logout ${registry}")
  }

  /**
   * Tag a Docker image with a new tag.
   *
   * @param config  map with:
   *   - image      : source image name (required)
   *   - tag        : new tag (required)
   *   - sourceTag  : source image tag (default 'latest')
   *   - registry   : registry URL (optional)
   */
  void tag(Map config = [:]) {
    def image = config.image
    if (!image) {
      error('dockerTag: "image" parameter is required')
    }
    def tag = config.tag
    if (!tag) {
      error('dockerTag: "tag" parameter is required')
    }

    def registry = config.registry
    def sourceTag = config.sourceTag ?: 'latest'
    def sourceImage = registry ? "${registry}/${image}:${sourceTag}" : "${image}:${sourceTag}"
    def targetImage = registry ? "${registry}/${image}:${tag}" : "${image}:${tag}"
    context.sh("docker tag ${sourceImage} ${targetImage}")
  }

  /**
   * Run a Docker container.
   *
   * @param config  map with:
   *   - image     : image to run (required)
   *   - command   : command to run inside the container (optional)
   *   - args      : additional docker run flags (optional)
   *   - detached  : run in detached mode (default false)
   *   - env       : map of environment variables (optional)
   *   - ports     : list of port mappings (optional)
   *   - volumes   : list of volume mounts (optional)
   *   - rm        : remove container after exit (default true)
   *   - name      : container name (optional)
   *   - retryAttempts     : retry count (default 1)
   *   - retryDelaySeconds : retry delay in seconds (default 0)
   */
  void run(Map config = [:]) {
    def utils = new Utils(context)
    def image = config.image
    if (!image) {
      error('dockerRun: "image" parameter is required')
    }

    def command = config.command ? "\"${config.command}\"" : ''
    def args = config.args ?: ''
    def detached = config.detached ? '-d' : ''
    def rm = config.rm != false ? '--rm' : ''
    def name = config.name ? "--name ${config.name}" : ''
    def attempts = config.retryAttempts ?: 1
    def delay = ((config.retryDelaySeconds ?: 0) * 1000L) as long

    def envFlags = ''
    if (config.env) {
      envFlags = config.env.collect { k, v -> "-e ${k}=${v}" }.join(' ')
    }

    def portFlags = ''
    if (config.ports) {
      portFlags = config.ports.collect { p -> "-p ${p}" }.join(' ')
    }

    def volumeFlags = ''
    if (config.volumes) {
      volumeFlags = config.volumes.collect { v -> "-v ${v}" }.join(' ')
    }

    utils.retry(attempts, delay) {
      context.sh(
        "docker run ${detached} ${rm} ${name} ${envFlags} ${portFlags} ${volumeFlags} ${args} ${image} ${command}".trim(),
      )
    }
  }

  /**
   * Execute a docker-compose command.
   *
   * @param config  map with:
   *   - command     : compose command (up, down, build, etc.) (required)
   *   - composeFile : path to docker-compose file (default 'docker-compose.yml')
   *   - args        : additional compose arguments (optional)
   *   - env         : map of environment variables (optional)
   *   - retryAttempts     : retry count (default 1)
   *   - retryDelaySeconds : retry delay in seconds (default 0)
   */
  void compose(Map config = [:]) {
    def utils = new Utils(context)
    def command = config.command
    if (!command) {
      error('dockerCompose: "command" parameter is required')
    }

    def composeFile = config.composeFile ?: 'docker-compose.yml'
    def args = config.args ?: ''
    def attempts = config.retryAttempts ?: 1
    def delay = ((config.retryDelaySeconds ?: 0) * 1000L) as long

    def envFlags = ''
    if (config.env) {
      envFlags = config.env.collect { k, v -> "-e ${k}=${v}" }.join(' ')
    }

    utils.retry(attempts, delay) {
      context.sh(
        "docker-compose ${envFlags} -f ${composeFile} ${command} ${args}".trim(),
      )
    }
  }

  /**
   * Clean up Docker resources (containers, images, volumes, networks, build cache).
   *
   * @param config  map with:
   *   - containers    : remove stopped containers (default true)
   *   - images        : remove dangling images (default true)
   *   - volumes       : remove unused volumes (default true)
   *   - networks      : remove unused networks (default true)
   *   - buildCache    : prune build cache (default false)
   *   - force         : skip confirmation prompt (default true)
   *   - pruneAll      : remove all unused images, not just dangling (default false)
   */
  void clean(Map config = [:]) {
    def utils = new Utils(context)
    def force = config.force != false
    def pruneAll = config.pruneAll ?: false

    if (force) {
      context.echo('[docker] Force cleanup enabled — skipping confirmation')
    }

    if (config.containers != false) {
      context.sh('docker container prune -f')
    }

    if (config.images != false) {
      if (pruneAll) {
        context.sh('docker image prune -af')
      } else {
        context.sh('docker image prune -f')
      }
    }

    if (config.volumes != false) {
      context.sh('docker volume prune -f')
    }

    if (config.networks != false) {
      context.sh('docker network prune -f')
    }

    if (config.buildCache) {
      context.sh('docker builder prune -f')
    }
  }

  /**
   * Inspect a Docker image or container and return the output.
   *
   * @param config  map with:
   *   - target  : image or container name/tag (required)
   *   - format  : output format (optional, default '{{json .}}')
   *
   * @return the raw inspect output as a String
   */
  String inspect(Map config = [:]) {
    def target = config.target
    if (!target) {
      error('dockerInspect: "target" parameter is required')
    }

    def format = config.format ?: '{{json .}}'
    return context.sh(
      script: "docker inspect --format '${format}' ${target}",
      returnStdout: true,
    ).trim()
  }

  /**
   * Login to AWS Elastic Container Registry (ECR).
   *
   * @param config  map with:
   *   - region        : AWS region (required)
   *   - accountId     : AWS account ID (optional, uses AWS CLI to resolve)
   *   - credentialsId : Jenkins credentials ID for AWS access keys (optional)
   *   - registry      : ECR registry URI (optional, auto-derived if accountId provided)
   */
  void loginEcr(Map config = [:]) {
    def region = config.region
    if (!region) {
      error('dockerLoginEcr: "region" parameter is required')
    }

    def accountId = config.accountId
    def registry = config.registry

    if (!registry && accountId) {
      registry = "${accountId}.dkr.ecr.${region}.amazonaws.com"
    }

    if (!registry) {
      error('dockerLoginEcr: "registry" or "accountId" parameter is required')
    }

    def credsId = config.credentialsId
    if (credsId) {
      context.withCredentials([[
        $class       : 'AmazonWebServicesCredentialsBinding',
        credentialsId: credsId,
        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY',
      ]]) {
        context.sh(
          "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${registry}",
        )
      }
    } else {
      context.sh(
        "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${registry}",
      )
    }
  }

  /**
   * Login to Google Container Registry (GCR).
   *
   * @param config  map with:
   *   - credentialsId : Jenkins credentials ID for GCR service account key (optional)
   *   - project       : GCP project ID (optional)
   *   - registry      : GCR registry URL (default gcr.io)
   */
  void loginGcr(Map config = [:]) {
    def registry = config.registry ?: 'gcr.io'
    def credsId = config.credentialsId
    def project = config.project

    if (credsId) {
      context.withCredentials([[
        $class       : 'GoogleServiceAccountKey',
        credentialsId: credsId,
        keyFileVariable: 'GOOGLE_APPLICATION_CREDENTIALS',
      ]]) {
        context.sh("gcloud auth configure-docker ${registry} --quiet")
      }
    } else if (project) {
      context.sh("gcloud auth configure-docker ${registry} --quiet")
    } else {
      context.sh("gcloud auth configure-docker ${registry} --quiet")
    }
  }
}