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

# install stabiv dependencies
RUN apt-get update && \
    apt-get install -y exiftool && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# copy custom screenrc for convenience
COPY ./tmux.conf /root/.tmux.conf

WORKDIR /root/workspace

# keep container running for development work
# (e.g. vscode remote development plugin)
# don't do this other than for development purposes
ENTRYPOINT ["tail", "-f", "/dev/null"]