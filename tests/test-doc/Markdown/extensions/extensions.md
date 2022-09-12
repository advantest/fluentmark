# Markdown syntax extensions

There are several Markdown syntax extensions and Markdown renderers (processors) that 
can handle those extensions.
We use pandoc for processing Markdown code, because supports most of our desired features.
Thus, our focus is on [pandoc's Markdown](https://pandoc.org/MANUAL.html#pandocs-markdown)
and [pandoc's Markdown extensions](https://pandoc.org/MANUAL.html#extensions).


## Heading identifiers

### Automatically generated heading identifiers

[Generated identifiers](auto-identifiers.md)


### Explicit heading identifiers

[Explicit identifiers](header-attributes.md)


## Code Blocks

### Code block attributes, anchors, and code highlighting

[Code block extensions](code-blocks.md)


## Lists

### Task lists

[Task lists](tasks.md)


## Tables

[Table support](tables.md)


## Math formula and expressions

[Math formula](math.md)


## Listing all available and active Markdown extensions

In a terminal, we can list all available Markdown extensions with the following command. 
Active extensions are preceded by a `+` character, inactive extensions are preceded 
by a `-`.

```sh
pandoc --list-extensions=markdown
```

Exemplary output (pandoc 2.18):

~~~
-abbreviations
+all_symbols_escapable
-angle_brackets_escapable
-ascii_identifiers
+auto_identifiers
-autolink_bare_uris
+backtick_code_blocks
+blank_before_blockquote
+blank_before_header
+bracketed_spans
+citations
-compact_definition_lists
+definition_lists
-east_asian_line_breaks
-emoji
+escaped_line_breaks
+example_lists
+fancy_lists
+fenced_code_attributes
+fenced_code_blocks
+fenced_divs
+footnotes
-four_space_rule
-gfm_auto_identifiers
+grid_tables
-gutenberg
-hard_line_breaks
+header_attributes
-ignore_line_breaks
+implicit_figures
+implicit_header_references
+inline_code_attributes
+inline_notes
+intraword_underscores
+latex_macros
+line_blocks
+link_attributes
-lists_without_preceding_blankline
-literate_haskell
-markdown_attribute
+markdown_in_html_blocks
-mmd_header_identifiers
-mmd_link_attributes
-mmd_title_block
+multiline_tables
+native_divs
+native_spans
-old_dashes
+pandoc_title_block
+pipe_tables
+raw_attribute
+raw_html
+raw_tex
-rebase_relative_paths
-short_subsuperscripts
+shortcut_reference_links
+simple_tables
+smart
+space_in_atx_header
-spaced_reference_links
+startnum
+strikeout
+subscript
+superscript
+task_lists
+table_captions
+tex_math_dollars
-tex_math_double_backslash
-tex_math_single_backslash
+yaml_metadata_block
~~~