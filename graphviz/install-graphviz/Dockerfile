#FROM redhat/ubi9:9.2
FROM ubuntu:22.04

#RUN yum update && \
#    yum --showduplicates list httpd | expand && \
#    yum install graphviz-2.44.0

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
            curl \
            ca-certificates \
            xz-utils
	#apt-get list graphviz && \
    #apt install -y --no-install-recommends graphviz=2.44.0-25
    #apt install -y --no-install-recommends graphviz=11.0.0
RUN mkdir -p /graphviz && \
    cd /graphviz && \
    curl -o graphviz.deb https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/11.0.0/ubuntu_22.04_graphviz-11.0.0-cmake.deb && \
    dpkg -i graphviz.deb
    #curl -o graphviz.tar.xz https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/11.0.0/ubuntu_22.04_graphviz-11.0.0-debs.tar.xz  && \
    #tar -xf graphviz.tar.xz && \
    #rm graphviz.tar.xz && \
WORKDIR /graphviz
#RUN dpkg -i *.deb

ENTRYPOINT ["/bin/bash"]