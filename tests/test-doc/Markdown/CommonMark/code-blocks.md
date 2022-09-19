### 4.4 Indented code blocks

4-spaces-indented code blocks:

    /**
     * This is some method described with Javadoc,
     * mentioning an attribute {@link #isCool} and a method {@link #doSomething(String)}.
     * 
     * @param cool if it has to be cool
     * @return <code>null</code>
     */
    private String doSomething(boolean cool) {
        return null;
    }

Tabs-indented code blocks:

    {"menu": {
      "id": "file",
      "value": "File",
      "popup": {
        "menuitem": [
          {"value": "New", "onclick": "CreateNewDoc()"},
          {"value": "Open", "onclick": "OpenDoc()"},
          {"value": "Close", "onclick": "CloseDoc()"}
        ]
      }
    }}



### 4.5 Fenced code blocks

```
<p>
  She has <span style="color:blue">blue</span> eyes.
</p>
```

~~~
>>[-]<<[->>+<<]
~~~

```yaml
# Known languages
- person:
    name: Steven McCloud
    job: Software Developer
    languages:
      - Java
      - Kotlin
      - JavaScript
```