# Short FluentMark features and syntax overview

<!--- Should here be an introduction? --->

## Images {#section-images}

![The FluentMark Logo PNG image (bitmap graphics)](../../doc/Logo110x80.png)

![Some SVG image (vector graphics)](../test-doc/Markdown/CommonMark/svg-example.svg)

**NEW:**
We found a way to adapt FluentMark and to add support for
PlantUML (.puml) file inclusion and on-demand generation of SVG files out of the included PlantUML files.
We no longer need to manually translate .puml files to .svg files and no longer need to commit them into our git repositories.
The syntax the same as for other image files:

```
![External PlantUML diagram file](PlantUML/classes.puml)
```

![External PlantUML diagram file](PlantUML/classes.puml)


## Links

* To Sections (anchors)
  * Link to a [section on this page](#section-images) (explicit anchor)
  * Link to [another section on this page](#code-snippets) (auto-generated anchor)
  * Link to a [section in another Markdown file](PlantUML/other.md#json)
* To Markdown files
  * Link to [another Markdown file](PlantUML/uml.md) in same project
  * Link to [another Markdown file](../test-doc/README.md) in another project
  * Link to [another Markdown file](../../README.md) outside the Eclipse workspace
* To source code files
  * Link to a [Java file](SomeClass.java)
  * Link to [an attribute in Java code](SomeClass.java#isCool)
  * Link to [a method in Java code](SomeClass.java#doSomething(String))
  * Link to a [PlantUML diagram file](PlantUML/classes.puml) (.puml)
* to other files
  * Link to a [PNG file](../test-doc/Markdown/CommonMark/Example.png)
  * Link to a [SVG file](../test-doc/Markdown/CommonMark/svg-example.svg)
  * Link to a [file PNG outside the Eclipse workspace](../../doc/Logo110x80.png)
  * Link to a [Java file outside the Eclipse workspace](../../net.certiv.fluentmark.ui/src/net/certiv/fluentmark/ui/views/FluentPreview.java)
* Web links
  * Link to [one web site](https://plantuml.com/en/)
  * Link to [another web site](https://pandoc.org/MANUAL.html#pandocs-markdown)


## Code snippets

Some log output in mono-space fonts:

~~~
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
~~~

Some Markdown code with syntax highlighting:

```markdown
# Title

Some text in a paragraph...

<!--
This is how we write comments in Markdown (that will be rendered into HTML source code).
-->

```

Another code snippet with syntax highlighting:

~~~java
public class CustomInterpreter extends ExpressionInterpreter {
    public Value calculate(Expression expression) {
        // do something interesting...
    }
}
~~~

## Tables

| Right aligned | Left aligned | Default | Centered          |
|--------------:|:-------------|---------|:-----------------:|
|  first cell   |  7           |    A    | longer text here  |
|  abc 123      |  194236723   |   Xyz   |   123             |

: Pipe table


## Mathematical expressions

$H_2O$ and $CO_2$ are chemical molecules.

$n! = \prod_{i=1..n}i$

Einstein's formula $E=mc^2$ is famous.

Formula in display mode (with LaTeX commands):

$$\sum_{i=1}^{n}=\frac{n(n+1)}{2}$$


Other expressions:
$$a_0+{1\over a_1+
      {1\over a_2+
        {1 \over a_3 + 
           {1 \over a_4}}}}$$

$$x_{1/2} = -\frac{p}{2}\pm \sqrt{\frac{p^2}{4}-q}$$


## PlantUML diagrams

### UML diagrams

@startuml

    interface List<E> {
       add(E): boolean
       remove(int): E
       get(int): E
       isEmpty(): boolean
       size(): int 
    }
    
    abstract class AbstractList<E>
    class ArrayList<E>
    class LinkedList<E>
    
    List <|.. AbstractList
    AbstractList <|-- ArrayList
    AbstractList <|-- LinkedList

@enduml

@startuml

 title: Links in diagrams

 class FluentPreview [[../../plugin/src/net/certiv/fluentmark/views/FluentPreview.java]]
 interface List [[https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/util/List.html]]

 FluentPreview -> ViewPart

@enduml

[See more UML diagram examples](PlantUML/uml.md)

### Non-UML diagrams

[See Non-UML diagram examples](PlantUML/other.md)

## DOT diagrams

~~~ dot

digraph M1{
    node[shape=box width=1.1]
    a -> b -> c;
    b -> d;
}

~~~


