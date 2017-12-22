## FluentMark

The full-featured Markdown editing environment for Eclipse.

### Features 

+ Multiple Markdown converters
    - direct support for [Pandoc](https://pandoc.org) and
      [BlackFriday](https://github.com/russross/blackfriday) markdown converters
    - built-in support for [CommonMark](https://github.com/jgm/CommonMark),
      [MarkdownJ](https://github.com/myabc/markdownj), 
      [PegDown](https://github.com/sirthias/pegdown) &
      [TxtMark](https://github.com/rjeschke/txtmark)
+ [Graphviz](http://www.graphviz.org/) DOT diagram generator support built-in
    - renders as SVG images embedded in exported Web documents
    - renders as PDF images embedded in exported PDF documents
+ Real-time browser-based preview of the generated document
    - DOT graphs are rendered and displayed in context in real-time
    - presentation is controlled by a preference selected stylesheet
        + preferences include multiple built-in stylesheet
        + allows external stylesheets to be selected from the filesystem
    - full code highlighting using [highlight.js](https://highlightjs.org/)
+ Spell check with quick-assist correction processor
    - can be enabled independent of the platform spell checker
+ Smart editing behaviors
    - table editor and formatter
    - text and list formatter
    - intelligent paragraph, list & blank line handling
    - full outline drag and drop support

### Screenshot

<figure>
<a href="http://www.certiv.net/updates/net.certiv.fluentmark.site/ScreenShot-0.9.png">
	<img src="http://www.certiv.net/updates/net.certiv.fluentmark.site/ScreenShot-0.9.png"
		alt="FluentMark v0.9 screenshot" width="800"></a><br/><br/>
	<figcaption>FluentMark v0.14</figcaption>
</figure>

---

### Install

**FluentMark** can be installed from the Certiv Tools update site at [www.certiv.net/updates](http://www.certiv.net/updates/).

The FluentMark _Preferences_ can then be found at `Window` => `Certiv Tools` => `FluentMark`  

To use **DOT**, install [Graphviz](http://www.graphviz.org/download.php). Then, in 
the FluentMark Converter preferences for DOT, browse to and select the `dot` executable. 


To use **BlackFriday**, install our customized [blackfriday-tool](https://github.com/grosenberg/blackfriday-tool) 
from its GitHub repository. A Windows 64-bit compiled executable is available for 
download: [blackfriday-tool.zip](http://www.certiv.net/updates/net.certiv.fluentmark.site/blackfriday-tool.zip). 
Then, in the FluentMark Converter preferences for BlackFriday, browse to and select 
the `blackfriday-tool` executable.

To use **Pandoc**, install [Pandoc](https://pandoc.org). Then, in the FluentMark 
Converter preferences for Pandoc, browse to and select the `pandoc` executable.

To use the **PDF** exporter, both *Pandoc* and a _LaTeX_ processor must be installed. 
Pandoc recommends [*MikTeX*](https://miktex.org/).

### Use

|Key         |Function                                          |
|:-----------|:-------------------------------------------------|
|Ctrl-Space  |Opens the template assist popup                   |
|Ctrl-B      |Toggles **bold** of selected text                 |
|Ctrl-I      |Toggles _italics_ of selected text                |
|Ctrl-/      |Toggles Markdown-style commenting of selected text|
|Ctrl-Shift-/|Toggles HTML-style commenting of selected text    |
|Ctrl-Shift-F|Format full page or selected text                 |

Inline Math is defined using single `$` open/close delimiters with no space after 
the opening delimiter and before the closing delimiter. Can be embedded in other 
markdown features.

Math Blocks are delimited using `$$` marks provided on the left margin.  The open 
delimiter must follow and the close must lead a blank line.

`Double-click` on a table to open the table editor. `Double-click` a cell to edit 
text. `Tab` and the arrow keys navigate between cells. `Return` to end cell editing.

### Requirements

Eclipse Oxygen+ JDK 1.8+

### Development Status

- Version 0.14
- In active development
