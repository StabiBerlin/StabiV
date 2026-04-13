FROM docker.io/library/ubuntu:24.04

ARG EXIFTOOL_VERSION=13.55
ARG PERL_VERSION=5.42.2

ENV DEBIAN_FRONTEND=noninteractive

# install build dependencies
RUN apt-get update && \
    apt-get install -y build-essential wget curl ca-certificates libssl-dev zlib1g-dev libbz2-dev libreadline-dev libncurses-dev \
       libgdbm-dev libdb-dev libexpat1-dev xz-utils patchelf unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# download perl
RUN wget -O perl.tar.gz "https://www.cpan.org/src/5.0/perl-${PERL_VERSION}.tar.gz"

# make statically linked perl
RUN tar -xvzf perl.tar.gz --no-same-owner && \
    cd perl-${PERL_VERSION} && \
    ./Configure -des \
      -Dprefix=/opt/perl-static \
      -Duseshrplib=false && \
    make -j$(nproc) && \
    make install

# add perl to path
ENV PATH="/opt/perl-static/bin:$PATH"

# install par packager
RUN curl -L https://cpanmin.us/ -o cpanm && \
    chmod +x cpanm && \
    ./cpanm Archive::Zip PAR PAR::Packer IO::String 

# download exiftool
RUN wget -O exiftool.zip "https://github.com/exiftool/exiftool/archive/refs/tags/${EXIFTOOL_VERSION}.zip"

# build exiftool into binary
RUN unzip -q exiftool.zip && \
    cd exiftool-* && \
    pp `awk '!/^#/ && !/Win32|Brotli/' pp_build_exe.args | tr '\n' ' '` && \
    mkdir /output && \
    mv exiftool.exe /bin/exiftool

# test exiftool works on ubuntu without any perl
FROM docker.io/library/ubuntu:24.04

COPY --from=0 /bin/exiftool /bin/exiftool

RUN exiftool -ver