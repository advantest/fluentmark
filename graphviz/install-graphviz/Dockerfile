#FROM redhat/ubi9:9.2
FROM ubuntu:22.10

#RUN yum update && \
#    yum --showduplicates list httpd | expand && \
#    yum install graphviz-2.44.0

RUN apt update && \
	apt list graphviz && \
    apt install -y --no-install-recommends graphviz=2.44.0-25

ENTRYPOINT ["/bin/bash"]