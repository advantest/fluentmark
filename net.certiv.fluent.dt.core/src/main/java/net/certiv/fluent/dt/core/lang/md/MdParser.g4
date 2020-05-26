parser grammar MdParser ;

options {
	tokenVocab = MdLexer ;
}

@header { 
	package net.certiv.fluent.dt.core.lang.md.gen;
}

page
	:	( yamlBlock	| htmlBlock | dotBlock  |  mathBlock
		| texBlock	| umlBlock	| codeBlock
		| header	| hrule
		| table	    | list
		| paragraph | definition
		| lnBlank	| lnBreak
		| comment	| err
		)*
	EOF
	;

yamlBlock : YAML_BLOCK ;
htmlBlock : HTML_BLOCK ; 
dotBlock  : DOT_BLOCK  ;
mathBlock : MATH_BLOCK ;
texBlock  : TEX_BLOCK  ; 
umlBlock  : UML_BLOCK  ;

codeBlock
	: CODE_BEG lang=WORD? style? VWS+ ( WORD | VWS )* CODE_END
	;

header
	: HASHES line HASH* style?
	| line ( EQUALS | DASHES )
	;

hrule
	: HRULE style?
	;

table 
	: tableRow* TABLE_DEF style? tableRow*
	;

tableRow
	: PIPE? ( line? PIPE )+ line? PIPE?
	;

list
	: listItem+
	;

listItem
	: listMark ( line lnBreak? )*
	;

listMark
	: mark=UNORDERED_MARK
	| mark=NUMBER_MARK
	| mark=PAREN_MARK
	| mark=UALPHA_MARK
	| mark=LALPHA_MARK
	;

definition
	: line ( COLON line ( lnBreak line )* )+
	;

paragraph
	: line ( lnBreak line )*
	;

line 
	: ( word | link )+ 
	;

link
	: ( IMAGE | LBRACK ) ( link | text ) LINK_SEP URL ( LDQUOTE text RDQUOTE )? RPAREN style?
	| ( IMAGE | LBRACK ) ( link | text ) REF_SEP text RBRACK style?
	| ( IMAGE | LBRACK ) text? RBRACK style?
	| ( IMAGE | LBRACK ) URL RBRACK style?
	| LBRACK text DEF_SEP URL ( LDQUOTE text RDQUOTE )?
	;

text : word+ ;

word
	: attrLeft* 
	  w= ( WORD		 |  RPAREN 
		 | CODE_SPAN |  MATH_SPAN
		 | UNICODE	 |  ENTITY
		 | HTML		 | TEX | URL
		 )
	  attrRight*
	;

style
	: LSTYLE 
		( CLASS WORD
		| ID WORD
		| WORD EQ ( QUOTE? WORD QUOTE? | MARK? WORD MARK? )
		)+ 
	  RSTYLE
	;

attrLeft  : LBOLD | LITALIC | LSTRIKE | LSPAN | LDSPAN | LDQUOTE | LSQUOTE ;
attrRight : RBOLD | RITALIC | RSTRIKE | RSPAN | RDSPAN | RDQUOTE | RSQUOTE ;

comment : COMMENT ;
lnBlank : LINE_BLANK ;
lnBreak : LINE_BREAK ;

err : ERR+ ; 
