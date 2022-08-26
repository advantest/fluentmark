## Tables

### [Extension: `simple_tables`](https://pandoc.org/MANUAL.html#extension-simple_tables)

Simple tables is the simplest form of the four table dialects.
It offers simple syntax and supports column alignment.

Examples:

---

  Right     Left     Center     Default
-------     ------ ----------   -------
     12     12        12            12
    123     123       123          123
      1     1          1             1

Table:  Table title: Simple table with different column alignments

---

-------     ------ ----------   -------
     12     12        12             12
    123     123       123           123
      1     1          1              1
-------     ------ ----------   -------

: Title: Table without header


### [Extension: `multiline_tables`](https://pandoc.org/MANUAL.html#extension-multiline_tables)

Multi-line tables support header and cell entries with multiple lines, but not cells 
that span multiple columns or rows.
The relative column with is more or less taken from the Markdown source code.

Examples:

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

Table: Here's the caption. It, too, may span
multiple lines.

---

----------- ------- --------------- -------------------------
   First    row                12.0 Example of a row that
                                    spans multiple lines.

  Second    row                 5.0 Here's another one. Note
                                    the blank line between
                                    rows.
----------- ------- --------------- -------------------------

: Here's a multi-line table without a header.


### [Extension: `grid_tables`](https://pandoc.org/MANUAL.html#extension-grid_tables)

Grid tables require a line with `=` symbols to separate the header from the table body.
In case of tables without a header, this can be replaced with regular `-` symbols.
Alignment is specified by placing colons (`:`) at the columns' boundaries in the line 
of separator symbols.

Examples:

---

: Sample grid table.

+---------------+---------------+--------------------+
| Fruit         | Price         | Advantages         |
+===============+===============+====================+
| Bananas       | $1.34         | - built-in wrapper |
|               |               | - bright color     |
+---------------+---------------+--------------------+
| Oranges       | $2.10         | - cures scurvy     |
|               |               | - tasty            |
+---------------+---------------+--------------------+

---

+---------------+---------------+--------------------+
| Right         | Left          | Centered           |
+==============:+:==============+:==================:+
| Bananas       | $1.34         | built-in wrapper   |
+---------------+---------------+--------------------+
Table: Alignment examples

---

+--------------:+:--------------+:------------------:+
| Right         | Left          | Centered           |
+---------------+---------------+--------------------+
: Header-less table with alignment


### [Extension: `pipe_tables`](https://pandoc.org/MANUAL.html#extension-pipe_tables)

Pipe tables require the cells in a row to be separated by pipes (`|`).
They also require a header (which could contain whitespace characters
to simulate header-less tables.)

Examples:

---

| Right | Left | Default | Center |
|------:|:-----|---------|:------:|
|   12  |  12  |    12   |    12  |
|  123  |  123 |   123   |   123  |
|    1  |    1 |     1   |     1  |

  : Demonstration of pipe table syntax.

---

fruit| price
-----|-----:
apple|2.05
pear|1.37
orange|3.09

---

| One | Two   |
|-----+-------|
| my  | table |
| is  | nice  |
