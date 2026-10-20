# 🚀 Jenkins Shared Library (`devops-shared`)

A generic, reusable [Jenkins Shared Library](https://www.jenkins.io/doc/book/pipeline/shared-pipelines/) that
provides common pipeline abstractions so you don't have to copy-paste boilerplate
across every job.

Built on the [project-template](https://github.com/gvatsal60/project-template) foundation and
licensed under **Apache 2.0**.

---

## Table of Contents

- [Features](#features)
- [Library Layout](#library-layout)
- [Quick Start](#quick-start)
- [Available Steps](#available-steps)
  - [checkoutCode](#checkoutcode)
  - [buildApp](#buildapp)
  - [runTests](#runtests)
  - [deploy](#deploy)
  - [waitForInput](#waitforinput)
  - [notify](#notify)
  - [dockerBuild](#dockerbuild)
  - [dockerPush](#dockerpush)
  - [dockerLogin](#dockerlogin)
  - [dockerLogout](#dockerlogout)
  - [dockerTag](#dockertag)
  - [dockerRun](#dockerrun)
  - [dockerCompose](#dockercompose)
  - [dockerClean](#dockerclean)
  - [dockerInspect](#dockerinspect)
  - [dockerLoginEcr](#dockerloginecr)
  - [dockerLoginGcr](#dockerlogingcr)
- [Background Classes](#background-classes)
- [Configuration](#configuration)
- [Jenkinsfile Example](#jenkinsfile-example)
- [Development](#development)
- [License](#license)

---

## Features

- **Build-tool agnostic** — supports **Maven** and **Gradle** through a single
  `buildApp` / `runTests` step.
- **Retry & timing** — all steps inherit retry and elapsed-time instrumentation
  from `Utils.retry` / `Utils.time`.
- **Unified notifications** — Slack + email via the `notify` step.
- **Git helpers** — checkout with changelog, git tagging on release.
- **Docker support** — build, push, login, tag, run, compose, clean, inspect, and registry-specific logins (ECR, GCR).
- **Centralised config** — `Config` provides sane defaults with per-call overrides.
- **Reusable & typed** — strongly-typed Groovy classes under `src/org/devops/`.

---

## Library Layout

```
jenkins-shared-library/
├── src/                       # Compiled Groovy/Java classes (importable)
│   └── org/devops/
│       ├── Utils.groovy        # logging, retry, timing
│       ├── Config.groovy       # config defaults + overrides
│       ├── Notification.groovy # Slack & email
│       ├── GitHelper.groovy    # checkout, tagging, changelogs
│       ├── BuildHelper.groovy  # build + test orchestration
│       └── Docker.groovy       # Docker build, push, login, run, compose, clean
├── vars/                      # Global steps callable from any Jenkinsfile
│   ├── checkoutCode.groovy
│   ├── buildApp.groovy
│   ├── runTests.groovy
│   ├── deploy.groovy
│   ├── waitForInput.groovy
│   ├── notify.groovy
│   ├── dockerBuild.groovy
│   ├── dockerPush.groovy
│   ├── dockerLogin.groovy
│   ├── dockerLogout.groovy
│   ├── dockerTag.groovy
│   ├── dockerRun.groovy
│   ├── dockerCompose.groovy
│   ├── dockerClean.groovy
│   ├── dockerInspect.groovy
│   ├── dockerLoginEcr.groovy
│   └── dockerLoginGcr.groovy
├── resources/                 # Non-Groovy resources
│   └── org/devops/library.json
├── Jenkinsfile.example        # Example consumer pipeline
├── README.md
└── LICENSE
```

---

## Quick Start

1. **Register the library in Jenkins:**

   *Manage Jenkins → Configure System → Global Pipeline Libraries*

   | Field            | Value                          |
   |------------------|--------------------------------|
   | Name             | `devops-shared`                |
   | Default version  | `main` (or your release tag) |
   | Source           | Git / GitHub / Bitbucket URL   |
   | Load implicitly  | *check* (optional)             |

2. **Use it in a Jenkinsfile:**

   ```groovy
   @Library('devops-shared') _

   pipeline {
     agent any
     stages {
       stage('Checkout') {
         steps { checkoutCode(url: 'https://github.com/org/repo.git') }
       }
       stage('Build')   { steps { buildApp() } }
       stage('Test')    { steps { runTests() } }
     }
   }
   ```

See [`Jenkinsfile.example`](Jenkinsfile.example) for a full, annotated pipeline.

---

## Available Steps

### `checkoutCode`

Checkout source code with retry and optional changelog.

```groovy
checkoutCode(
  url            : 'https://github.com/org/repo.git',
  credentialsId  : 'git-cred',      // optional
  branch         : 'develop',       // default 'main'
  changelog      : true,            // default true
  retryAttempts  : 3,               // default 1
  retryDelaySeconds : 10,           // default 0
)
```

### `buildApp`

Run a build with the configured tool.

```groovy
buildApp(
  tool    : 'maven',            // 'maven' (default) | 'gradle'
  goals   : 'clean package',    // maven default | 'clean build' for gradle
  extraArgs: ['-DskipTests'],
  retryAttempts: 2,
  failOnError: true,
)
```

### `runTests`

Run the test suite, then publish JUnit + coverage reports.

```groovy
runTests(
  tool : 'maven',      // default
  goals: 'test',
  extraTestArgs: '-Dparallel=methods',
)
```

### `deploy`

Execute a deploy command, tag git, and notify on completion.

```groovy
deploy(
  command       : './scripts/deploy.sh staging',
  version       : '1.0.42',           // optional — triggers git tag
  url           : 'https://github.com/org/repo.git',  // required for tagging
  credentialsId : 'git-cred',          // for pushing tags
  tag           : true,               // default true when version set
  slackChannel  : '#builds',
  emailRecipients: 'team@example.com',
)
```

### `waitForInput`

Wait for manual approval inside a timeout.

```groovy
waitForInput(
  message       : 'Approve production deploy?',
  ok            : 'Approve',            // default 'Approve'
  submitter     : 'dev-team',           // Jenkins user/group
  timeoutMinutes: 1440,                 // default 1440 (24h)
)
```

### `notify`

Send a status notification (Slack + email). Controlled by `Config` defaults:
`notifyOnFailure` (default `true`), `notifyOnSuccess` (default `false`).

```groovy
notify(status: 'SUCCESS', message: 'Deploy finished')

// shorthand
notify currentBuild.result ?: 'SUCCESS', 'Build done'
```

### `dockerBuild`

Build a Docker image with optional tags, build args, and registry login.

```groovy
dockerBuild(
  image             : 'myapp',
  tags              : ['1.0.0', 'latest'],
  registry          : 'registry.example.com',
  registryCredentialsId : 'docker-cred',
  buildArgs         : [JAVA_VERSION: '17'],
  noCache           : true,
  retryAttempts     : 2,
)
```

### `dockerPush`

Push a Docker image tag to a registry.

```groovy
dockerPush(
  image             : 'myapp',
  tag               : '1.0.0',
  registry          : 'registry.example.com',
  registryCredentialsId : 'docker-cred',
)
```

### `dockerLogin`

Login to a Docker registry using Jenkins credentials or plain username/password.

```groovy
dockerLogin(
  registry          : 'registry.example.com',
  credentialsId     : 'docker-cred',
)

// or with explicit credentials
dockerLogin(
  registry  : 'registry.example.com',
  username  : 'deployer',
  password  : 's3cret',
)
```

### `dockerLogout`

Logout from a Docker registry.

```groovy
dockerLogout(registry: 'registry.example.com')
dockerLogout  // defaults to Docker Hub
```

### `dockerTag`

Tag a Docker image with a new tag.

```groovy
dockerTag(image: 'myapp', tag: '1.0.0')
dockerTag(image: 'myapp', tag: '1.0.0', registry: 'registry.example.com')
```

### `dockerRun`

Run a Docker container with optional ports, volumes, env vars, and detach mode.

```groovy
dockerRun(
  image     : 'myapp',
  command   : 'npm start',
  detached  : true,
  ports     : ['8080:8080'],
  volumes   : ['/data:/app/data'],
  env       : [NODE_ENV: 'production'],
  rm        : true,
  name      : 'myapp-container',
)
```

### `dockerCompose`

Execute a docker-compose command.

```groovy
dockerCompose(command: 'up', args: '-d')
dockerCompose(command: 'down', composeFile: 'docker-compose.prod.yml')
dockerCompose(command: 'build', env: [BUILD_NUMBER: env.BUILD_NUMBER])
```

### `dockerClean`

Clean up Docker resources (stopped containers, dangling images, unused volumes, networks, build cache).

```groovy
dockerClean  // remove stopped containers, dangling images, unused volumes and networks
dockerClean(buildCache: true, pruneAll: true)  // aggressive cleanup
dockerClean(containers: false, images: true)  // selective cleanup
```

### `dockerInspect`

Inspect a Docker image or container and return the output.

```groovy
def info = dockerInspect(target: 'myapp:latest')
def id = dockerInspect(target: 'myapp:latest', format: '{{.Id}}')
```

### `dockerLoginEcr`

Login to AWS Elastic Container Registry (ECR).

```groovy
dockerLoginEcr(region: 'us-east-1', accountId: '123456789012')
dockerLoginEcr(
  region        : 'us-east-1',
  registry      : '123456789012.dkr.ecr.us-east-1.amazonaws.com',
  credentialsId : 'aws-creds',
)
```

### `dockerLoginGcr`

Login to Google Container Registry (GCR).

```groovy
dockerLoginGcr(credentialsId: 'gcr-service-account')
dockerLoginGcr(project: 'my-gcp-project')
dockerLoginGcr(registry: 'us.gcr.io', credentialsId: 'gcr-service-account')
```

---

## Background Classes

| Class           | Responsibility                                              |
|-----------------|-------------------------------------------------------------|
| `Utils`         | `info`/`warn`/`error` logging, `time`, `retry`, `get`      |
| `Config`        | Default map with override support; typed accessors          |
| `GitHelper`     | Checkout, changelog generation, git tagging                 |
| `BuildHelper`   | Tool dispatch (Maven/Gradle), test result & coverage publish |
| `Notification`  | Slack + email backends with graceful degradation            |
| `Docker`        | Build, push, login, tag, run, compose, clean, inspect, ECR/GCR login |

All classes implement `Serializable` and accept the owning `Script` context so
they work correctly across pipeline restarts.

---

## Configuration

Every step merges its call-time arguments on top of `Config.DEFAULTS`.
The full set of overridable keys:

| Key                 | Default     | Description                              |
|---------------------|-------------|------------------------------------------|
| `branch`            | `main`      | Default git branch                       |
| `buildTool`         | `maven`     | `maven` or `gradle`                      |
| `failOnError`       | `true`      | Fail build on compile/test error         |
| `failOnWarning`     | `false`     | Fail build on warnings                   |
| `notifyOnFailure`   | `true`      | Notify on `FAILURE`                      |
| `notifyOnSuccess`   | `false`     | Notify on `SUCCESS`                      |
| `slackChannel`      | `#builds`   | Default Slack channel                    |
| `emailRecipients`   | `''`        | Default email recipients                 |
| `gitCredentialsId`  | `''`        | Default git credentials ID               |
| `timeoutMinutes`    | `30`        | Default pipeline timeout                 |
| `retryAttempts`     | `1`         | Default retry count for network ops      |
| `retryDelaySeconds` | `0`         | Default delay between retries            |
| `cleanWorkspace`    | `false`     | Clean workspace before checkout          |

---

## Jenkinsfile Example

A complete annotated example lives in [`Jenkinsfile.example`](Jenkinsfile.example).
It demonstrates checkout → build → test → manual approval → deploy with
notifications on every outcome.

---

## Development

This repository uses [pre-commit](https://pre-commit.com/) to enforce linting
across many languages. To set up locally:

```sh
pip install pre-commit
pre-commit install
pre-commit run --all-files
```

The dev container (see `.devcontainer/`) provisions a ready-to-code environment.

### Project tooling

| Tool        | Purpose                          |
|-------------|----------------------------------|
| Groovy      | Jenkins pipeline & library code  |
| pre-commit  | Multi-language linting hooks     |
| markdownlint| README / doc style enforcement   |

---

## License

Apache License 2.0 — see [LICENSE](LICENSE).
