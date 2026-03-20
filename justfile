platform := "linux/amd64"
registry := "container-registry.prod.nla.gov.au"
pom_version := `xmlstarlet sel -t -v "/_:project/_:version" -n pom.xml`

default:
    @just --list

# Build all modules
build:
    mvn package -DskipTests

test:
    mvn test

# Push all container images
push-all version=pom_version:
    @just push ui {{version}}
    @just push gatherer {{version}}
    @just push delivery {{version}}

# Push a container image
push module="ui" version=pom_version:
    podman build {{module}} -t nla/pandas-{{module}}:{{version}} --platform {{platform}} -f {{module}}/Dockerfile.nla
    podman tag nla/pandas-{{module}}:{{version}} {{registry}}/nla/pandas-{{module}}:{{version}}
    podman push {{registry}}/nla/pandas-{{module}}:{{version}}

# Build, push and deploy a module
deploy module="ui" env="devel" version=pom_version:
    @just build
    @just push {{module}} {{version}}
    kubectl rollout restart statefulset/pandas-{{module}} -n pandas-{{env}}
    kubectl rollout status statefulset/pandas-{{module}} -n pandas-{{env}}

shell module="ui" env="devel":
    kubectl exec -it $(kubectl get pods -l app.kubernetes.io/name=pandas-{{module}} -n pandas-{{env}} -o jsonpath='{.items[0].metadata.name}') -n pandas-{{env}} -- /bin/bash
