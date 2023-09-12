# Build Graphviz from sources

## With Docker based on Ubuntu

1. `cd build-graphviz`
2. `docker build -t graphviz:2.38 -f Dockerfile2 .`
3. test with `docker run --name test -t -i --rm graphviz:2.38`, then `dot -version`


## With Docker based on RHEL