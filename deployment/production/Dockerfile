FROM docker.io/library/ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# install developer basics
RUN apt-get update && \
    apt-get install -y bash git git-lfs tmux vim dnsutils jq && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# install node / npm for auto code formatting via prettier
RUN apt-get update && \
    apt-get install -y --no-install-recommends nodejs npm && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# install java 21 and maven
RUN apt-get update && \
    apt-get -y install default-jre-headless default-jdk maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# install image validation dependencies
RUN apt-get update && \
    apt-get install -y exiftool && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# copy files
COPY bin /root/workspace/bin
COPY code /root/workspace/code
COPY documentation /root/workspace/documentation
COPY CHANGELOG.md /root/workspace/CHANGELOG.md
COPY LICENSE /root/workspace/LICENSE

# create empty pdf files to avoid maven assembly error
RUN touch /root/workspace/CHANGELOG.pdf && \
    touch /root/workspace/documentation/userguide/README.pdf && \
    touch /root/workspace/documentation/getting-started/Linux.pdf && \
    touch /root/workspace/documentation/getting-started/Windows.pdf

# build jar file
RUN cd /root/workspace/bin && bash package.sh

# -----------------------------------------------------------------------------
FROM docker.io/library/ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# install runtime requirements (java 21 and exiftool)
RUN apt-get update && \
    apt-get -y install default-jre-headless exiftool && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /root/workspace

COPY --from=0 /root/workspace/code/cli/target/runtimeDependencies /root/build/jars
COPY --from=0 /root/workspace/code/cli/target/imagevalidation-cli-1.0.0-SNAPSHOT.jar /root/build/jars/imagevalidation-cli-1.0.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-cp", "/root/build/jars/*", "de.spkberlin.imagevalidation.cli.entrypoint.Main"]