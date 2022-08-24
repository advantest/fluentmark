# Markdown basic syntax

Here, we collect examples for the most common Markdown syntax, i.e. language features 
covered by the [CommonMark Specification](https://commonmark.org)
and the majority of Markdown tools.

Based on [CommonMark Specification Version 0.30 (2021-06-19)](https://spec.commonmark.org/0.30/)

## 4 Leaf blocks

### 4.1 Thematic breaks

Horizontal lines

***

text

---

text

___

text

--------------------------


### 4.2 ATX headings

[Headings](atx-headings.md)


### 4.3 Setext headings

[Headings](setext-headings.md)


### 4.4 Indented code blocks & 4.5 Fenced code blocks

[Code blocks](code-blocks.md)


### 4.6 HTML blocks

[HTML code in Markdown](html.md)


### 4.7 Link reference definitions

Some link reference definitions in action:

[GFM]: https://github.github.com/gfm/ "GitHub-flavored Markdown"
[GLFM]: https://docs.gitlab.com/ee/user/markdown.html
  "GitLab-flavored Markdown"
[CommonMark]: https://commonmark.org/

[GFM], [GLFM],
[CommonMark]


### 4.8 Paragraphs & 4.9 Blank lines

Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.

At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.



 Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.
              At vero eos et accusam et justo duo dolores et ea rebum.
       Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.



## 5 Container blocks
 
### 5.1 Block quotes

> #### Foo
> Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
> sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,
> sed diam voluptua.

>> Blub
>> bla
>> Ba boom
>>>> blub


### 5.2 List items & 5.3 Lists

* Lists can be unordered,
* ordered,
  * and hierarchical
  * with many items
* Also consider indentation in source code

Another example:


1.  A paragraph
    with two lines.

        indented code

    > A block quote.
    
2.  Some other text
      i. with sub items
      i. automatically numbered
         one after the other
         
3.  Third thing

One more example:

* I need to buy
    - new shoes
    - a coat
    - a plane ticket


## 6 Inlines

TODO