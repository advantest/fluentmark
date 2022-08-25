# Documentation Example for Test Purposes

We test various Markdown, PlantUML, DOT, and other language features grouped in separate 
files. This way, it is easier to switch between different tool versions, git branches, 
etc. and testing the feature groups separately.


## Features


### Markdown

An overview of Markdown basic syntax, syntax extensions, and renderers (processors) are documented 
in the [Markdown Guide](https://www.markdownguide.org/).

We plant to use [pandoc](https://pandoc.org) for rendering Markdown source code, thus, we're interested in 
Markdown extensions that are available in pandoc. The pandoc documentation includes 
the describtion of [pandoc's Markdown](https://pandoc.org/MANUAL.html#pandocs-markdown).

* [Markdown basic syntax (structured according to CommonMark Spec.)](Markdown/CommonMark/basic.md)
* [Markdown extensions (available in pandoc)](Markdown/extensions/extensions.md)


### PlantUML diagrams

[PlantUML diagrams syntax](PlantUML/plantuml.md)


### DOT diagrams

[DOT diagrams syntax](DOT/dot.md)

