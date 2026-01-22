# based on https://github.com/plantuml/plantuml-server/blob/master/Dockerfile.tomcat

FROM maven:3.9-eclipse-temurin-21
#FROM eclipse-temurin:21-jdk

# Install pandoc besides other useful tools
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        pandoc \
        fonts-noto-cjk \
        libgd3 \
        && \
    rm -rf /var/lib/apt/lists/*

# Build Graphviz from source because there are no binary distributions for recent versions
ARG GRAPHVIZ_VERSION=11.0.0
ARG GRAPHVIZ_BUILD_DIR=/tmp/graphiz-build
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        jq \
        libexpat1-dev \
        libgd-dev \
        zlib1g-dev \
        libltdl-dev \
        ca-certificates \
        libtool \
        automake \
        autoconf \
        bison \
        flex
RUN mkdir -p $GRAPHVIZ_BUILD_DIR && \
    cd $GRAPHVIZ_BUILD_DIR

    #GRAPHVIZ_VERSION=${GRAPHVIZ_VERSION:-$(curl -s https://gitlab.com/api/v4/projects/4207231/releases/ | jq -r '.[] | .name' | sort -V -r | head -1)} && \
    #curl -o graphviz.tar.gz https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/${GRAPHVIZ_VERSION}/graphviz-${GRAPHVIZ_VERSION}.tar.gz && \
RUN curl -o graphviz.tar.gz https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/${GRAPHVIZ_VERSION}/graphviz-${GRAPHVIZ_VERSION}.tar.gz && \
    tar -xzf graphviz.tar.gz && \
    cd graphviz-$GRAPHVIZ_VERSION && \
    #./autogen.sh && \
    ./configure && \
    make && \
    make install
RUN apt-get remove -y \
        build-essential \
        jq \
        libexpat1-dev \
        libgd-dev \
        zlib1g-dev \
#        libltdl-dev \
#        ca-certificates \
#        libtool \
#        automake \
#        autoconf \
#        bison \
#        flex \
        && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf $GRAPHVIZ_BUILD_DIR

ENTRYPOINT ["/bin/bash"]