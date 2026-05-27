<!-- see https://shields.io/badges -->

![GitHub License](https://img.shields.io/github/license/advantest/fluentmark)
[![Java CI with Maven/Tycho](https://github.com/advantest/fluentmark/actions/workflows/ci-docker.yml/badge.svg?branch=main)](https://github.com/advantest/fluentmark/actions/workflows/ci-docker.yml)

# FluentMark Advantest Edition (AE)

A full-featured Markdown editing environment for Eclipse IDE.
FluentMark was originally developed by [Certiv Analytics](https://www.certiv.net/index.html) / [Gerald Rosenberg](https://github.com/grosenberg) and
was modified for [Advantest Europe GmbH](https://www.advantest.com/en/) to better satisfy company-specific needs.

This work is forked from [https://github.com/grosenberg/Fluentmark](https://github.com/grosenberg/Fluentmark).

## Features

+ Choice of Markdown converter
    - support for the [flexmark](https://github.com/advantest/flexmark-java) converter (preferred) **(Advantest Edition only)**
    - support for the [Pandoc](https://pandoc.org) converter
+ Real-time preview
    - smooth, reactively rendered HTML display, using [Vue.js](https://vuejs.org/)
    - navigation from source code to preview and back **(Advantest Edition only)**
    - improved navigation support (e.g. following links in preview opens same file in editor) **(Advantest Edition only)**
    - Zoom images in the preview **(Advantest Edition only)**
    - stylesheet controlled presentation
        + multiple built-in stylesheets
        + local custom/user-defined stylesheets
+ PDF export using Pandoc
    - custom/user-defined LaTeX page template support
+ LaTex/Math presentation using [MathJax](https://www.mathjax.org/)
+ Code highlighting in code blocks using [highlight.js](https://highlightjs.org/)
+ Diagram rendering
    - UML diagrams using the [PlantUML](https://plantuml.com/) language
    <!-- - Graph diagrams using the [Graphviz DOT](http://www.graphviz.org/) language -->
    - all diagram previews are rendered in real-time
    - exported Web and PDF documents embed the diagrams as scalable SVG images
+ Markdown code validation and error reporting **(Advantest Edition only)**
    - Check link targets (web links, links to sections, links to files). Do the linked files, sections, or web sites exist?
    - Check anchors (section identifiers). Are identifiers unique? Do they contain illegal characters?
    - Check image references (find missing image files)
    - Add additional validations using extension points (e.g. check links to tickets in your intra-net)
+ Spell check with quick-assist correction processor
+ Various code assist features
    - Code completion for code templates
    - Code completion for anchors (section identifiers) and links to sections **(Advantest Edition only)**, etc.
    - Code assist for creating paths to files, either using a file selection dialog or by step-wise completing the file path using code proposals **(Advantest Edition only)**
+ Refactoring operations **(Advantest Edition only)**
    - Extract a PlantUML code block to a linked *.puml file or in-line such a file as a code block
    - Replace SVG images with on-demand rendered PlantUML diagram files
+ Smart editing behaviors, including intelligent paragraph, list & blank line handling
+ Table editor
+ Text, list and table formatter
+ Support for TODO and FIXME tasks in Markdown code **(Advantest Edition only)**
+ Outline view with drag-and-drop support
    - "Hide all but the sections" filter **(Advantest Edition only)**
+ Extended Markdown language **(Advantest Edition only)**
    - PlantUML code blocks are first-class citizens, no need to surround them in fenced code blocks, i.e. everything between `@startuml` and `@enduml` is rendered as a PlantUML diagram
    - Fenced code blocks with the `plantuml` language are automatically rendered as a diagram (not as a code block or text)
    - Using *.puml files as images, e.g. `![Some diagram](classes.puml)` (they are rendered on demand)
    - [Footnote support](https://github.com/vsch/flexmark-java/wiki/Extensions#footnotes)
    <!-- - Links to Java members (methods or fields), e.g. [important method](path/to/ClassName.java#getSomething(int, boolean, Character[], List<Map<K,V>>)) -->

## Screenshots

<figure>
  <img src="./doc/ScreenShot.png" alt="FluentMark Dot graph">
  <figcaption>1. Dot Graph</figcaption>
</figure>

<figure>
  <img src="./doc/ScreenShot1.png" alt="FluentMark Sequence diagram">
  <figcaption>2. Sequence Diagram</figcaption>
</figure>

## Installation & Use

Install the latest version from our Eclipse update site (p2 repository): [https://advantest.github.io/fluentmark/](https://advantest.github.io/fluentmark/).
(In Eclipse, select the menu Help -> Install New Software...,
then paste the update site URL into the text field and press enter, select the features to be installed and press the Finish button.)

Requires Eclipse 2025-03 or newer & JDK 21+.


Preferences ---
- `Window`&rarr;`FluentMark`

Pandoc converter ---
- Install [Pandoc](https://pandoc.org). The `pandoc` executable can then be selected from the local filesystem 
  on the Pandoc Converter preference page.

PDF export ---
- Both *Pandoc* and a _LaTeX_ processor must be installed. Pandoc recommends [*MikTeX*](https://miktex.org/).

DOT graphics ---
- Install [Graphviz](http://www.graphviz.org/download.php). The `dot` executable can then be selected 
  on the Converter preference page.

UML diagrams ---
- The basic PlantUml jar is built-in. Diagrams other than sequence diagrams require DOT graphics. If 
  `Graphviz` is installed in a non-default directory, set the `GRAPHVIZ_DOT` environment variable to 
  the actual installation directory.

### Keys

|Key             |Function                                          |
|:---------------|:-------------------------------------------------|
|Ctrl-Space      |Opens the template assist popup                   |
|Ctrl-1          |Spell check quick correct                         |
|Ctrl-b          |Toggles **bold** of selected text                 |
|Ctrl-i          |Toggles _italics_ of selected text                |
|Ctrl-/          |Toggles Markdown-style commenting of selected text|
|Ctrl-Shift-/    |Toggles HTML-style commenting of selected text    |
|Ctrl-Shift-f    |Format - full page or selection                   |
|Ctrl-Shift-Alt-f|Format - with unwrapped text                      |

### Math

In-line Math uses single `$` open/close delimiters. Can be embedded in other markdown features.

The opening `$` _must_ have a non-space character immediately right.  The closing `$` _must_ have a non-space 
character immediately left and _must_ be followed immediately by a non-digit. 

Math blocks are delimited using double `$` (*i.e.*, `$$`) marks at the left margin. The open delimiter 
must follow a blank line and the close delimiter must lead a blank line.

### Table Editor

`Double-click` on a table to open the table editor. While in the editor, `double-click` a cell to edit 
text. `Tab` and arrow keys will navigate between cells. `Return` to end cell editing.

## Support

Open an [issue on Github](https://github.com/advantest/fluentmark/issues). 

Provide as much information as applicable, including the plugin version number, any error message encountered, 
and a minimal example of the Markdown text at issue.

### Resources

1. Markdown Syntax
    - [Pandoc's Markdown](https://pandoc.org/MANUAL.html#pandocs-markdown)
    - [GitHub Flavored Markdown](https://github.github.com/gfm/)
    - [Daring Fireball Markdown](https://daringfireball.net/projects/markdown/syntax)
1. [TEX Commands available in MathJax](http://www.onemathematicalcat.org/MathJaxDocumentation/TeXSyntax.htm)
1. [PlantUML Language Specification](https://plantuml.com/en/guide)
1. [Dot Language Man Page](http://www.graphviz.org/pdf/dot.1.pdf)
1. Pandoc Latex Templates (for PDF generation):
    - [Starter Templates](https://github.com/jez/pandoc-starter)
    - [Letter Template](https://github.com/aaronwolen/pandoc-letter)
    - [Notes Oriented Template](https://github.com/Wandmalfarbe/pandoc-latex-template)
    - [Collection of Templates](https://github.com/lauritzsh/pandoc-markdown-template)

## License

[EPL v1](license.md)

