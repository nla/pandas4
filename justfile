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

# Remove Maven release-plugin metadata from a previous attempt.
release-clean:
    mvn -B release:clean

# Prepare and perform a local Maven release.
# Requires ~/.m2/settings.xml to provide credentials for:
#   nla-public-releases
#   nla-public-snapshots
# Example:
#   just release-maven 4.13.3 4.13.4-SNAPSHOT
release-maven version next_version:
    echo "{{version}}" | rg -qv 'SNAPSHOT$'
    echo "{{next_version}}" | rg -q 'SNAPSHOT$'
    @just release-clean
    mvn -B -Dresume=false -DreleaseVersion={{version}} -DdevelopmentVersion={{next_version}} release:prepare release:perform

# Build and push a release image from the Maven release checkout.
# This uses the jars produced by `release:perform` in `target/checkout`.
release-push module version:
    #!/usr/bin/env bash
    set -euo pipefail
    version='{{version}}'
    module='{{module}}'
    echo "$version" | rg -qv 'SNAPSHOT$'
    checkout_dir="$PWD/target/checkout"

    case "$module" in
      ui)
        jar_name="pandas-admin.jar"
        ;;
      gatherer)
        jar_name="pandas4-gatherer.jar"
        ;;
      *)
        echo "Unsupported release image module: $module" >&2
        exit 1
        ;;
    esac

    if [ ! -f "$checkout_dir/pom.xml" ]; then
      echo "Missing $checkout_dir/pom.xml. Run 'just release-maven $version <next-snapshot-version>' first." >&2
      exit 1
    fi

    if [ "$(xmlstarlet sel -t -v '/_:project/_:version' -n "$checkout_dir/pom.xml")" != "$version" ]; then
      echo "target/checkout is not at release version $version" >&2
      exit 1
    fi

    if [ ! -f "$checkout_dir/$module/Dockerfile.nla" ]; then
      echo "Missing $checkout_dir/$module/Dockerfile.nla" >&2
      exit 1
    fi

    if [ ! -f "$checkout_dir/$module/target/$jar_name" ]; then
      echo "Missing $checkout_dir/$module/target/$jar_name. Run 'just release-maven $version <next-snapshot-version>' first." >&2
      exit 1
    fi

    echo "Using release:perform output from $checkout_dir/$module/target/$jar_name"
    podman build "$checkout_dir/$module" -t "nla/pandas-${module}:${version}" --platform {{platform}} -f "$checkout_dir/$module/Dockerfile.nla"
    podman tag "nla/pandas-${module}:${version}" {{registry}}/"nla/pandas-${module}:${version}"
    podman push {{registry}}/"nla/pandas-${module}:${version}"

# Push release images for modules that use custom Dockerfiles.
# delivery is released via Nexus artifact + base image in ArgoCD, so it is excluded.
release-images version:
    @just release-push ui {{version}}
    @just release-push gatherer {{version}}

# Print sed commands to update the prod ArgoCD values files in the ops-managed repo.
release-argocd version:
    echo "{{version}}" | rg -qv 'SNAPSHOT$'
    printf '%s\n' \
      "sed -E 's/^version: .*/version: {{version}}/' ~/src/argocd-bss/.gitops/pandas-ui/prod/values.yaml" \
      "sed -E 's/^version: .*/version: {{version}}/' ~/src/argocd-bss/.gitops/pandas-gatherer/prod/values.yaml" \
      "sed -E 's/^version: .*/version: {{version}}/' ~/src/argocd-bss/.gitops/pandas-delivery/prod/values.yaml"

# Full release helper: publish Maven release artifacts, push release images, then print ArgoCD follow-up.
# Example:
#   just release 4.13.3 4.13.4-SNAPSHOT
release version next_version:
    @just release-maven {{version}} {{next_version}}
    @just release-images {{version}}
    @just release-argocd {{version}}

shell module="ui" env="devel":
    kubectl exec -it $(kubectl get pods -l app.kubernetes.io/name=pandas-{{module}} -n pandas-{{env}} -o jsonpath='{.items[0].metadata.name}') -n pandas-{{env}} -- /bin/bash
