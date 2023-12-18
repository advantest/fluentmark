# First Header

We can find infos about the [Markdown basic syntax](https://www.markdownguide.org/basic-syntax/) in the internet.

## Second-Level Header 

Lorem ipsum...
Referencing [Section 1](subsection/sub1.md).

### Third-Level Header

Bla blub...

Here comes something **really** important.
We have to *emphasize* that text formatting is useful.
We're going to user ***Markdown***, we think.

> Here's a
> multi-line text
> quote
>
> with multiple paragraphs
>
>> and a nested quote

What about lists?


1. First item
1. Second item
1. Third item
1. Fourth item

unordered lists:

* First item
* Second item
* Third item
* Fourth item

---

another option:

- First item
- Second item
- Third item
- Fourth item

## Images

![Tux, the Linux mascot](img/tux.png)

## Code Snippets

We often mention `code extracts` in our texts.
But sometimes we use

    some
        code
        blocks
    that
        are
        rendered
    differently
    
So let's do that.


# Extended Markdown (GFM)

## Tables

| Syntax      | Description |
| ----------- | ----------- |
| Header      | Title       |
| Paragraph   | Text        |

---

| Syntax | Description |
| --- | ----------- |
| Header | Title |
| Paragraph | Text |

---

  Right     Left     Center     Default
-------     ------ ----------   -------
     12     12        12            12
    123     123       123          123
      1     1          1             1

---

-------------------------------------------------------------
 Centered   Default           Right Left
  Header    Aligned         Aligned Aligned
----------- ------- --------------- -------------------------
   First    row                12.0 Example of a row that
                                    spans multiple lines.

  Second    row                 5.0 Here's another one. Note
                                    the blank line between
                                    rows.
-------------------------------------------------------------

---

+---------------+---------------+--------------------+
| Fruit         | Price         | Advantages         |
+===============+===============+====================+
| Bananas       | $1.34         | - built-in wrapper |
|               |               | - bright color     |
+---------------+---------------+--------------------+
| Oranges       | $2.10         | - cures scurvy     |
|               |               | - tasty            |
+---------------+---------------+--------------------+

## Syntax Highlighting

```json
{
  "firstName": "John",
  "lastName": "Smith",
  "age": 25
}
```

# PlantUML

@startuml
    Alice -> Bob  : Authentication Request
    Bob --> Alice : Authentication Response

    Alice ->   Bob : Second authentication Request
    Alice <--o Bob : Second authentication Response
@enduml 

# DOT

~~~ dot

digraph M1{
	node[shape=box width=1.1]
	a -> b -> c;
    b -> d;
}

~~~

digraph X1 {
    a-> b -> c -> a 
}


