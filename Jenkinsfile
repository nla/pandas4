nlaBuild steps: this,
    deployToNexus: true,
    applicationName: "pandas-ui",
    jdk: 'JDK 17',
    mavenProfile: 'jenkins',
    ignoreSonar: true

def isMaster = env.BRANCH_NAME == 'master'
def isTag = env.TAG_NAME || env.BRANCH_NAME ==~ /^tags\/.+/

if (isMaster || isTag) {
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
                "IMAGE_VERSION=${version}"
            ]) {
                sh '''
                    set -eu

                    mvn -B -Pjenkins -DskipTests package

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
