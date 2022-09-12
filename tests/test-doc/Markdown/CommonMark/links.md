### 6.3 Links

Links can have a link text, a destination, and a title
(see [CommonMark specification](https://spec.commonmark.org/0.30/#links)). All parts 
can be omitted.

[Back to parent section](basic.md)

[Back to root document](../../README.md "README.md")

[Google](https://google.com "search")


#### Reference links

*[Full reference links][full]* have a link text and refer to a link label 
defined somewhere else in the document.

*Collapsed reference links* are missing a link text. Instead, they begin with the link 
label followed by a pair of empty square brackets. Example: [collapsed][]

*Shortcut reference links* only have a link label and are equivalent to collapsed reference 
links. Example: [shortcut]

[full]: https://spec.commonmark.org/0.30/#full-reference-link "CommonMark Spec."
[collapsed]: https://spec.commonmark.org/0.30/#collapsed-reference-link "CommonMark Spec."
[shortcut]: https://spec.commonmark.org/0.30/#shortcut-reference-link "CommonMark Spec."
