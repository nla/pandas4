FROM registry.access.redhat.com/ubi9/openjdk-17 AS build
COPY --chown=default:default . ./pandas4/
RUN --mount=id=m2,type=cache,target=/home/default/.m2,uid=185,gid=185 cd pandas4 && mvn -pl ui -am clean package -Dmaven.test.skip

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime
COPY --from=build /home/default/pandas4/ui/target/pandas-admin.jar .
CMD java -Xms128m -Xmx1g -jar pandas-admin.jar
