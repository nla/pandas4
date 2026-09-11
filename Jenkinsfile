def isMaster = env.BRANCH_NAME == 'master'
def isTag = env.TAG_NAME || env.BRANCH_NAME ==~ /^tags\/.+/
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
                            --tag "$CONTAINER_REGISTRY/nla/pandas-ui:$IMAGE_VERSION" \
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
                                sh '''podman push "$CONTAINER_REGISTRY/nla/pandas-ui:$IMAGE_VERSION"'''
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
