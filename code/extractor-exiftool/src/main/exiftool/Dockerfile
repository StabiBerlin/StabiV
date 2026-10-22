FROM docker.io/library/ubuntu:22.04 as build

ARG EXIFTOOL_VERSION=13.59
ARG PERL_VERSION=5.42.2

ENV DEBIAN_FRONTEND=noninteractive

# install build dependencies
RUN apt-get update && \
    apt-get install -y build-essential perl libperl-dev libpar-packer-perl curl wget unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# install par packager
RUN curl -L https://cpanmin.us/ -o cpanm && \
    chmod +x cpanm && \
    ./cpanm --notest Archive::Zip PAR PAR::Packer IO::String 

# download exiftool
RUN wget -O exiftool.zip "https://github.com/exiftool/exiftool/archive/refs/tags/${EXIFTOOL_VERSION}.zip"

# build exiftool into binary
RUN unzip -q exiftool.zip && \
    cd exiftool-* && \
    pp --clean `awk '!/^#/ && !/Win32|Brotli/' pp_build_exe.args | tr '\n' ' '` && \
    mv exiftool.exe /usr/bin/exiftool

# ----------------- test --------------------------

# test exiftool works on rockylinux 9 without any perl
FROM docker.io/library/rockylinux:9 as rocky9
COPY --from=build /usr/bin/exiftool /usr/bin/exiftool
RUN dnf install -y libxcrypt-compat && dnf clean all
RUN exiftool -ver

# test exiftool works on fedora 43 without any perl
FROM docker.io/library/fedora:43 as fedora43
COPY --from=rocky9 /usr/bin/exiftool /usr/bin/exiftool
RUN dnf install -y libxcrypt-compat && dnf clean all
RUN exiftool -ver

# test exiftool works on fedora 44 without any perl
FROM docker.io/library/fedora:44 as fedora44
COPY --from=fedora43 /usr/bin/exiftool /usr/bin/exiftool
RUN dnf install -y libxcrypt-compat && dnf clean all
RUN exiftool -ver

# test exiftool works on ubuntu 22.04 without any perl
FROM docker.io/library/ubuntu:22.04 as ubuntu2204
COPY --from=fedora44 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver

# test exiftool works on ubuntu 23.10 without any perl
FROM docker.io/library/ubuntu:23.10 as ubuntu2310
COPY --from=ubuntu2204 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver

# test exiftool works on ubuntu 24.04 without any perl
FROM docker.io/library/ubuntu:24.04 as ubuntu2404
COPY --from=ubuntu2310 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver

# test exiftool works on ubuntu 25.10 without any perl
FROM docker.io/library/ubuntu:25.10 as ubuntu2510
COPY --from=ubuntu2404 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver

# test exiftool works on ubuntu 26.04 without any perl
FROM docker.io/library/ubuntu:26.04 as ubuntu2604
COPY --from=ubuntu2510 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver

# test exiftool works on debian 12 without any perl
FROM docker.io/library/debian:12 as debian12
COPY --from=ubuntu2604 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver

# test exiftool works on debian 13 without any perl
FROM docker.io/library/debian:13 as debian13
COPY --from=debian12 /usr/bin/exiftool /usr/bin/exiftool
RUN exiftool -ver