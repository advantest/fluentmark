
### [Extension: `fenced_code_attributes`](https://pandoc.org/MANUAL.html#extension-fenced_code_attributes)

Fenced code blocks can use attributes, classes, and anchors.
Using these, we can, for example, activate syntax highlighting for a certain language,
add identifiers that can be used to create links pointing to the code block,
and add line numbering.

Examples:


#### Code block with json syntax highlighting:

```json
{
  "firstName": "John",
  "lastName": "Smith",
  "age": 25
}
```

#### Code block with identifier / anchor

~~~ {#java-example}
public void setVisible(boolean visible) {
    this.visible = visible;
    Group frame = (Group) comp.getParent();
    frame.setVisible(visible);
}
~~~


#### Code block with line numbering beginning with 42

For some reason, line numbering seems not to work with pandoc and FluentMark.

~~~ {#numbered .java .number-lines .line-anchors startFrom="42"}
public void setVisible(boolean visible) {
    this.visible = visible;
    Group frame = (Group) comp.getParent();
    frame.setVisible(visible);
}
~~~

#### Syntax highlighting support

There is a list of languages that pandoc has syntax highlighting support for. You can 
print that languages using the following command in a terminal.

```bash
pandoc --list-highlight-languages
```

Exemplary output (pandoc 2.18):

```
abc
actionscript
ada
agda
apache
asn1
asp
ats
awk
bash
bibtex
boo
c
changelog
clojure
cmake
coffee
coldfusion
comments
commonlisp
cpp
cs
css
curry
d
default
diff
djangotemplate
dockerfile
dot
doxygen
doxygenlua
dtd
eiffel
elixir
elm
email
erlang
fasm
fortranfixed
fortranfree
fsharp
gcc
glsl
gnuassembler
go
graphql
groovy
hamlet
haskell
haxe
html
idris
ini
isocpp
j
java
javadoc
javascript
javascriptreact
json
jsp
julia
kotlin
latex
lex
lilypond
literatecurry
literatehaskell
llvm
lua
m4
makefile
mandoc
markdown
mathematica
matlab
maxima
mediawiki
metafont
mips
modelines
modula2
modula3
monobasic
mustache
nasm
nim
noweb
objectivec
objectivecpp
ocaml
octave
opencl
orgmode
pascal
perl
php
pike
postscript
povray
powershell
prolog
protobuf
pure
purebasic
python
qml
r
raku
relaxng
relaxngcompact
rest
rhtml
roff
ruby
rust
sass
scala
scheme
sci
scss
sed
sgml
sml
spdxcomments
sql
sqlmysql
sqlpostgresql
stan
stata
swift
systemverilog
tcl
tcsh
texinfo
toml
typescript
verilog
vhdl
xml
xorg
xslt
xul
yacc
yaml
zsh
```