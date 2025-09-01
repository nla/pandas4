FROM registry.access.redhat.com/ubi9/openjdk-17 AS build
COPY --chown=default:default . ./pandas4/
RUN --mount=id=m2,type=cache,target=/home/default/.m2,uid=185,gid=185 cd pandas4 && mvn -pl gatherer -am clean package -Dmaven.test.skip

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime
USER root
RUN microdnf install -y tar gzip && microdnf clean all
RUN rpm --nodeps -i https://archives.fedoraproject.org/pub/archive/fedora/linux/releases/37/Everything/$(arch)/os/Packages/h/httrack-3.49.2-14.fc37.$(arch).rpm
RUN python3 -m ensurepip && python3 -m pip install pywb
USER 185
COPY --from=build /home/default/pandas4/gatherer/target/pandas4-gatherer.jar .
CMD java -Xms128m -Xmx1g -jar pandas4-gatherer.jar
