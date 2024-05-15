# Build Graphviz from sources

## With Docker based on Ubuntu

1. `docker build -t graphviz:11.0 -f build-graphviz/Dockerfile-ubuntu-11 build-graphviz`
   (or `docker build -t graphviz:2.38 -f build-graphviz/Dockerfile-ubuntu-238 build-graphviz`)
2. test with `docker run --name graphviz -t -i --rm --entrypoint /bin/bash graphviz:11.0`, then `dot -version`


## Translate PlantUML files to SVG using the built docker image

1. `docker run -it --rm --name graphviz -v ./tests:/input graphviz:11.0`
   (or `docker run -it --rm --name graphviz -v ./tests:/input graphviz:2.38`)