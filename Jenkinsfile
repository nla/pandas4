def isMaster = env.BRANCH_NAME == 'master'
def isTag = env.TAG_NAME || env.BRANCH_NAME ==~ /^tags\/.+/

properties([
    disableConcurrentBuilds(abortPrevious: true)
])

def builds = [:]

builds['Maven build and Nexus'] = {
    nlaBuild steps: this,
        deployToNexus: true,
        applicationName: "pandas-ui",
        jdk: 'JDK 17',
        mavenProfile: 'jenkins',
        ignoreSonar: true
}

if (isMaster || isTag) {
    builds['Build and push OCI images'] = {
        node('spade') {
            stage('Build and push OCI images') {
                checkout scm

                def pom = readMavenPom file: 'pom.xml'
                def version = pom.version
                def sourceCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                def uiImageVersion = isMaster ? "master-${sourceCommit}" : version

                if (isTag) {
                    def tag = env.TAG_NAME ?: env.BRANCH_NAME.substring('tags/'.length())
                    def expectedVersion = tag.startsWith('v') ? tag.substring(1) : tag
                    if (version != expectedVersion) {
                        error("Tag ${tag} does not match POM version ${version}")
                    }
                }

                withEnv([
                    'CONTAINER_REGISTRY=container-registry.prod.nla.gov.au',
                    "IMAGE_VERSION=${version}",
                    "UI_IMAGE_VERSION=${uiImageVersion}",
                    'JAVA_HOME=/usr/lib/jvm/java-17',
                    'PATH+JAVA=/usr/lib/jvm/java-17/bin',
                    'NO_PROXY=localhost,127.0.0.1,.nla.gov.au',
                    'no_proxy=localhost,127.0.0.1,.nla.gov.au'
                ]) {
                    sh '''
                        set -eu

                        java -version
                        mvn -version
                        mvn -B -Pjenkins -DskipTests -pl ui,gatherer -am package

                        podman build \
                            --platform linux/amd64 \
                            --file ui/Dockerfile.nla \
                            --tag "$CONTAINER_REGISTRY/nla/pandas-ui:$UI_IMAGE_VERSION" \
                            ui

                        podman build \
                            --platform linux/amd64 \
                            --file gatherer/Dockerfile.nla \
                            --tag "$CONTAINER_REGISTRY/nla/pandas-gatherer:$IMAGE_VERSION" \
                            gatherer
                    '''

                    withCredentials([usernamePassword(
                        credentialsId: 'harbor-pandas-pusher',
                        usernameVariable: 'HARBOR_USERNAME',
                        passwordVariable: 'HARBOR_PASSWORD'
                    )]) {
                        sh '''
                            set +x
                            printf '%s' "$HARBOR_PASSWORD" | podman login \
                                --username "$HARBOR_USERNAME" \
                                --password-stdin \
                                "$CONTAINER_REGISTRY"
                        '''

                        try {
                            retry(3) {
                                sh '''podman push "$CONTAINER_REGISTRY/nla/pandas-ui:$UI_IMAGE_VERSION"'''
                            }
                            retry(3) {
                                sh '''podman push "$CONTAINER_REGISTRY/nla/pandas-gatherer:$IMAGE_VERSION"'''
                            }
                        } finally {
                            sh '''podman logout "$CONTAINER_REGISTRY" || true'''
                        }
                    }
                }
            }
        }
    }
}

parallel builds

if (isMaster) {
    node('spade') {
        stage('Deploy pandas-ui to devel') {
            checkout scm

            def sourceCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
            def imageVersion = "master-${sourceCommit}"

            dir('argocd-deploy') {
                deleteDir()

                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/talos']],
                    userRemoteConfigs: [[
                        credentialsId: 'argocd-gitlab-writer',
                        url: 'git@gitlab.nla.gov.au:nla/argocd.git'
                    ]]
                ])

                withEnv(["IMAGE_VERSION=${imageVersion}"]) {
                    sh '''
                        set -eu

                        sed -i -E \
                            "s/^version: .*/version: ${IMAGE_VERSION}/" \
                            .gitops/pandas-ui/devel/values.yaml

                        test "$(sed -n 's/^version: //p' .gitops/pandas-ui/devel/values.yaml)" = "$IMAGE_VERSION"
                        git diff --check

                        if git diff --quiet -- .gitops/pandas-ui/devel/values.yaml; then
                            echo "pandas-ui devel already references ${IMAGE_VERSION}"
                            exit 0
                        fi

                        git config user.name 'pandas-ui deployment bot'
                        git config user.email 'pandas-ui-deploy@nla.gov.au'
                        git add .gitops/pandas-ui/devel/values.yaml
                        git commit -m "pandas-ui/devel: deploy ${IMAGE_VERSION}"
                    '''

                    sshagent(credentials: ['argocd-gitlab-writer']) {
                        sh '''
                            set -eu
                            git fetch origin talos
                            if ! git rebase origin/talos; then
                                git rebase --abort || true
                                exit 1
                            fi
                            git push origin HEAD:talos
                        '''
                    }
                }
            }
        }
    }
}
