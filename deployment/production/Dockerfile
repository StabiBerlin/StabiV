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

# coyp files
COPY bin /root/workspace/bin
COPY code /root/workspace/code

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

COPY --from=0 /root/workspace/code/cli/target/imagevalidation-cli-0.1.0-SNAPSHOT.jar /root/build/main.jar

ENTRYPOINT ["java", "-jar", "/root/build/main.jar"]