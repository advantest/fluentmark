Headings

# h1 Heading
## h2 Heading
### h3 Heading
#### h4 Heading
##### h5 Heading
###### h6 Heading


Emphasis

**bold**
__bold__
*italic*
_italic_
~~strikethrough~~


Lists
*  unordered
-  unordered
+  unordered
1. ordered


Comments

<!---
This is a hidden comment - will not be rendered into the HTML source
TODO Adapt colors
--->

<!--
This is a visible comment - will be rendered in the HTML source, 
though invisible on the page
FIXME Implement that neat feature
-->


Horizontal rules (three consecutive symbols)

___	
---	
***	


Links

A [web](https://plantuml.com) link.
An ![image](http://octodex.github.com/images/minion.png) link;
A ![PlantUML diagram](../../diagrams/classes.puml) link;
A [reference][id] link.

[id]: https://octodex.github.com/images/dojocat.jpg  "The Dojocat"


Paragraphs

Lorem ipsum dolor sit amet, graecis denique ei vel, at duo primis mandamus. 
Et legere ocurreret pri, animal tacimates complectitur ad cum. Cu eum inermis 
inimicus efficiendi. Labore officiis his ex, soluta officiis concludaturque 
ei qui, vide sensibus vim ad.


Code & code blocks

Simple `inline code` in body copy.

    Indented: code blocks are very useful for developers and other people who 
    look at code or other things that are written in plain text. 


~~~
fenced code block (preferred)
~~~

```
fenced code block
```


HTML

<figure>
<a href="http://www.certiv.net/updates/net.certiv.fluentmark.site/ScreenShot-0.8.png">
	<img src="http://www.certiv.net/updates/net.certiv.fluentmark.site/ScreenShot-0.8.png"
		alt="FluentMark v0.8 screenshot" 
		align="left" width="800"></a>
	<figcaption>FluentMark v0.8</figcaption>
</figure>


PlantUML

@startuml
  ' import style
  <style file=https://plantuml.com/stylesheets/v1/plantuml-style.css>
  
  /' Define some highlighting color
     for usage in this diagram '/
  !define HIGHLIGHT #orange
  
  class FluentEditor <<EditorPart>> [[https://somewhere.com/some/link]]
  interface ITextEditor HIGHLIGHT
  
  FluentEditor ..|> ITextEditor
  FluentEditor --> 1..1 "outline" FluentOutlinePage
  
  footer Rendered with PlantUML version %version()

@enduml


DOT

~~~ dot

digraph M1{
    node[shape=box width=1.1]
    a -> b -> c;
    b -> d;
}

~~~


Math

Einstein's formula $E=mc^2$ is famous.

Formula in display mode (with LaTeX commands):

$$\sum_{i=1}^{n}=\frac{n(n+1)}{2}$$


Tables

| Right | Left | Default | Center |
|------:|:-----|---------|:------:|
|   12  |  12  |    12   |    12  |
|  123  |  123 |   123   |   123  |

: Pipe table


Quotes

> Here is a quote. What this is should be self explanatory. 
> Quotes are automatically indented when they are used.

