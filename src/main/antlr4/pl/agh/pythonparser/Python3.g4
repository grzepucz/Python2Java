grammar Python3;

tokens { INDENT, DEDENT }

@lexer::members {

  // A queue where extra tokens are pushed on (see the NEWLINE lexer rule).
  private java.util.LinkedList<Token> tokens = new java.util.LinkedList<>();

  // The stack that keeps track of the indentation level.
  private java.util.Stack<Integer> indents = new java.util.Stack<>();

  // The amount of opened braces, brackets and parenthesis.
  private int opened = 0;

  // The most recently produced token.
  private Token lastToken = null;

  @Override
  public void emit(Token t) {
    super.setToken(t);
    tokens.offer(t);
  }

  @Override
  public Token nextToken() {

    // Check if the end-of-file is ahead and there are still some DEDENTS expected.
    if (_input.LA(1) == EOF && !this.indents.isEmpty()) {

      // Remove any trailing EOF tokens from our buffer.
      for (int i = tokens.size() - 1; i >= 0; i--) {
        if (tokens.get(i).getType() == EOF) {
          tokens.remove(i);
        }
      }

      // First emit an extra line break that serves as the end of the statement.
      this.emit(commonToken(Python3Parser.NEWLINE, "\n"));

      // Now emit as much DEDENT tokens as needed.
      while (!indents.isEmpty()) {
        this.emit(createDedent());
        indents.pop();
      }

      // Put the EOF back on the token stream.
      this.emit(commonToken(Python3Parser.EOF, "<EOF>"));
    }

    Token next = super.nextToken();

    if (next.getChannel() == Token.DEFAULT_CHANNEL) {
      // Keep track of the last token on the default channel.
      this.lastToken = next;
    }

    return tokens.isEmpty() ? next : tokens.poll();
  }

  private Token createDedent() {
    CommonToken dedent = commonToken(Python3Parser.DEDENT, "");
    dedent.setLine(this.lastToken.getLine());
    return dedent;
  }

  private CommonToken commonToken(int type, String text) {
    int stop = this.getCharIndex() - 1;
    int start = text.isEmpty() ? stop : stop - text.length() + 1;
    return new CommonToken(this._tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, start, stop);
  }

  // Calculates the indentation of the provided spaces, taking the
  // following rules into account:
  //
  // "Tabs are replaced (from left to right) by one to eight spaces
  //  such that the total number of characters up to and including
  //  the replacement is a multiple of eight [...]"
  //
  //  -- https://docs.python.org/3.1/reference/lexical_analysis.html#indentation
  static int getIndentationCount(String spaces) {

    int count = 0;

    for (char ch : spaces.toCharArray()) {
      switch (ch) {
        case '\t':
          count += 8 - (count % 8);
          break;
        default:
          // A normal space char.
          count++;
      }
    }

    return count;
  }

  boolean atStartOfInput() {
    return super.getCharPositionInLine() == 0 && super.getLine() == 1;
  }
}

/*
 * parser rules
 */


operators
: and_op
| or_op
;

and_op
:AND
;

or_op
:OR
;

single_input
 : NEWLINE
 | simple_stmt
 | compound_stmt NEWLINE
 ;


file_input
 : ( NEWLINE | stmt )* EOF
 ;

decorator
 : '@' dotted_name ( '(' arglist? ')' )? NEWLINE
 ;

decorators
 : decorator+
 ;

decorated
 : decorators ( classdef | funcdef )
 ;

funcdef
 : DEF NAME parameters ( '->' test )? ':' suite
 ;

parameters
 : '(' typedargslist? ')'
 ;

typedargslist
 : tfpdef ( '=' test )? ( ',' tfpdef ( '=' test )? )* ( ',' ( '*' tfpdef? ( ',' tfpdef ( '=' test )? )* ( ',' '**' tfpdef )? 
                                                            | '**' tfpdef 
                                                            )? 
                                                      )?
 | '*' tfpdef? ( ',' tfpdef ( '=' test )? )* ( ',' '**' tfpdef )? 
 | '**' tfpdef
 ;

tfpdef
 : NAME ( ':' test )?
 ;

/// varargslist: (vfpdef ['=' test] (',' vfpdef ['=' test])* [','
///       ['*' [vfpdef] (',' vfpdef ['=' test])* [',' '**' vfpdef] | '**' vfpdef]]
///     |  '*' [vfpdef] (',' vfpdef ['=' test])* [',' '**' vfpdef] | '**' vfpdef)
varargslist
 : vfpdef ( '=' test )? ( ',' vfpdef ( '=' test )? )* ( ',' ( '*' vfpdef? ( ',' vfpdef ( '=' test )? )* ( ',' '**' vfpdef )? 
                                                            | '**' vfpdef 
                                                            )? 
                                                      )?
 | '*' vfpdef? ( ',' vfpdef ( '=' test )? )* ( ',' '**' vfpdef )?
 | '**' vfpdef
 ;

vfpdef
 : NAME
 ;

stmt
 : simple_stmt 
 | compound_stmt
 ;

simple_stmt
 : small_stmt ( ';' small_stmt )* ';'? NEWLINE
 ;

small_stmt
 : expr_stmt
 | flow_stmt
 | import_stmt
 ;

expr_stmt
 : testlist_star_expr ( augassign ( yield_expr | testlist) 
                      | ( '=' ( yield_expr| testlist_star_expr ) )*
                      )
 ;           

testlist_star_expr
 : ( test | star_expr ) ( ',' ( test |  star_expr ) )* ','?
 ;

augassign
 : '+=' 
 | '-=' 
 | '*=' 
 | '@='
 | '/=' 
 | '%=' 
 | '&=' 
 | '|=' 
 | '^=' 
 | '<<=' 
 | '>>=' 
 | '**=' 
 | '//='
 ;

flow_stmt
 : break_stmt 
 | continue_stmt 
 | return_stmt 
 | raise_stmt 
 | yield_stmt
 ;

break_stmt
 : BREAK
 ;

continue_stmt
 : CONTINUE
 ;

return_stmt
 : RETURN testlist?
 ;

yield_stmt
 : yield_expr
 ;

raise_stmt
 : RAISE ( test ( FROM test )? )?
 ;

import_stmt
 : import_name 
 | import_from
 ;

import_name
 : IMPORT dotted_as_names
 ;

import_from
 : FROM ( ( '.' | '...' )* dotted_name 
        | ('.' | '...')+ 
        )
   IMPORT ( '*' 
          | '(' import_as_names ')' 
          | import_as_names
          )         
 ;

import_as_name
 : NAME ( AS NAME )?
 ;

dotted_as_name
 : dotted_name ( AS NAME )?
 ;

import_as_names
 : import_as_name ( ',' import_as_name )* ','?
 ;

dotted_as_names
 : dotted_as_name ( ',' dotted_as_name )*
 ;

dotted_name
 : NAME ( '.' NAME )*
 ;

compound_stmt
 : if_stmt 
 | while_stmt 
 | for_stmt 
 | try_stmt
 | funcdef 
 | classdef 
 | decorated
 | elif_stmt
 | else_stmt
 ;


except_clause_suite
 : suite
 ;

try_suite
 : suite
 ;

finally_suite
 : suite
 ;

try_else_suite
 : suite
 ;



/// if_stmt: 'if' test ':' suite ('elif' test ':' suite)* ['else' ':' suite]
if_stmt
// : IF test ':' suite ( ELIF test ':' suite )* ( ELSE ':' suite )?
 //;
 /*:IF test ':' suite // elif_stmt
 ;*/

 :IF test (operators test)* ':' suite // elif_stmt
 ;

elif_stmt
 : ELIF (test (operators test)* ':' suite)* // else_stmt
 ;

 else_stmt
 :ELSE (':' suite)?
 ;
/// while_stmt: 'while' test ':' suite ['else' ':' suite]
while_stmt
 : WHILE test ':' suite ( ELSE ':' suite )?
 ;

for_stmt
 : FOR exprlist IN testlist ':' suite ( ELSE ':' suite )?
 ;

try_stmt
 : TRY ':' try_suite ( ( except_clause ':' except_clause_suite )+
                   ( ELSE ':' try_else_suite )?
                   ( FINALLY ':' finally_suite )?
                 | FINALLY ':' finally_suite
                 )
 ;
/// # NB compile.c makes sure that the default except clause is last
/// except_clause: 'except' [test ['as' NAME]]
except_clause
 : EXCEPT ( test ( AS NAME )? )?
 ;

suite
 : simple_stmt 
 | NEWLINE INDENT stmt+ DEDENT
 ;

test
 : or_test ( IF or_test ELSE test )?
 ;

test_nocond
 : or_test
 ;

/*or_test
 : and_test ( OR and_test )*
 ;*/
 or_test
  : not_test ( OR not_test )*
  ;

/*and_test
 : not_test ( AND not_test )*
 ;*/

not_test
 : NOT not_test 
 | comparison
 ;

comparison
 : star_expr ( comp_op star_expr )*
 ;

comp_op
 : '<'
 | '>'
 | '=='
 | '>='
 | '<='
 | '<>'
 | '!='
 | IN
 | NOT IN
 | IS
 | IS NOT
 ;

star_expr
 : '*'? expr
 ;

expr
 : xor_expr ( '|' xor_expr )*
 ;

xor_expr
 : and_expr ( '^' and_expr )*
 ;

and_expr
 : shift_expr ( '&' shift_expr )*
 ;

shift_expr
 : arith_expr ( '<<' arith_expr 
              | '>>' arith_expr 
              )*
 ;

arith_expr
 : term ( '+' term
        | '-' term 
        )*
 ;

term
 : factor ( '*' factor
          | '/' factor
          | '%' factor 
          | '//' factor 
          | '@' factor
          )*
 ;

factor
 : '+' factor 
 | '-' factor 
 | '~' factor 
 | power
 ;

power
 : atom trailer* ( '**' factor )?
 ;

atom
 : '(' ( yield_expr | testlist_comp )? ')' 
 | '[' testlist_comp? ']'
 | NAME 
 | number 
 | str+ 
 | '...' 
 | NONE
 | TRUE
 | FALSE
 ;

testlist_comp
 : test ( comp_for 
        | ( ',' test )* ','? 
        )
 ;

trailer
 : '(' arglist? ')'
 | '.' NAME
 ;

exprlist
 : star_expr ( ',' star_expr )* ','?
 ;

testlist
 : test ( ',' test )* ','?
 ;

classdef
 : CLASS NAME ( '(' arglist? ')' )? ':' suite
 ;

arglist
 : ( argument ',' )* ( argument ','?
                     | '*' test ( ',' argument )* ( ',' '**' test )?
                     | '**' test
                     )
 ;

/// # The reason that keywords are test nodes instead of NAME is that using NAME
/// # results in an ambiguity. ast.c makes sure it's a NAME.
/// argument: test [comp_for] | test '=' test  # Really [keyword '='] test
argument
 : test comp_for? 
 | test '=' test
 ;

comp_iter
 : comp_for 
 | comp_if
 ;

comp_for
 : FOR exprlist IN or_test comp_iter?
 ;

comp_if
 : IF test_nocond comp_iter?
 ;

yield_expr
 : YIELD yield_arg?
 ;

yield_arg
 : FROM test 
 | testlist
 ;

str
 : STRING_LITERAL
 | BYTES_LITERAL
 ;

number
 : integer
 | FLOAT_NUMBER
 | IMAG_NUMBER
 ;

integer
 : DECIMAL_INTEGER
 | OCT_INTEGER
 | HEX_INTEGER
 | BIN_INTEGER
 ;

/*
 * lexer rules
 */

DEF : 'def';
RETURN : 'return';
RAISE : 'raise';
FROM : 'from';
IMPORT : 'import';
AS : 'as';
IF : 'if';
ELIF : 'elif';
ELSE : 'else';
WHILE : 'while';
FOR : 'for';
IN : 'in';
TRY : 'try';
FINALLY : 'finally';
EXCEPT : 'except';
OR : 'or';
AND : 'and';
NOT : 'not';
IS : 'is';
NONE : 'None';
TRUE : 'True';
FALSE : 'False';
CLASS : 'class';
YIELD : 'yield';
CONTINUE : 'continue';
BREAK : 'break';

NEWLINE
 : ( {atStartOfInput()}?   SPACES
   | ( '\r'? '\n' | '\r' | '\f' ) SPACES?
   )
   {
     String newLine = getText().replaceAll("[^\r\n\f]+", "");
     String spaces = getText().replaceAll("[\r\n\f]+", "");
     int next = _input.LA(1);

     if (opened > 0 || next == '\r' || next == '\n' || next == '\f' || next == '#') {
       // If we're inside a list or on a blank line, ignore all indents, 
       // dedents and line breaks.
       skip();
     }
     else {
       emit(commonToken(NEWLINE, newLine));

       int indent = getIndentationCount(spaces);
       int previous = indents.isEmpty() ? 0 : indents.peek();

       if (indent == previous) {
         // skip indents of the same size as the present indent-size
         skip();
       }
       else if (indent > previous) {
         indents.push(indent);
         emit(commonToken(Python3Parser.INDENT, spaces));
       }
       else {
         // Possibly emit more than 1 DEDENT token.
         while(!indents.isEmpty() && indents.peek() > indent) {
           this.emit(createDedent());
           indents.pop();
         }
       }
     }
   }
 ;

NAME
 : ID_START ID_CONTINUE*
 ;

/// stringliteral   ::=  [stringprefix](shortstring | longstring)
/// stringprefix    ::=  "r" | "R"
STRING_LITERAL
 : [uU]? [rR]? ( SHORT_STRING | LONG_STRING )
 ;

/// bytesliteral   ::=  bytesprefix(shortbytes | longbytes)
/// bytesprefix    ::=  "b" | "B" | "br" | "Br" | "bR" | "BR"
BYTES_LITERAL
 : [bB] [rR]? ( SHORT_BYTES | LONG_BYTES )
 ;

/// decimalinteger ::=  nonzerodigit digit* | "0"+
DECIMAL_INTEGER
 : NON_ZERO_DIGIT DIGIT*
 | '0'+
 ;

/// octinteger     ::=  "0" ("o" | "O") octdigit+
OCT_INTEGER
 : '0' [oO] OCT_DIGIT+
 ;

/// hexinteger     ::=  "0" ("x" | "X") hexdigit+
HEX_INTEGER
 : '0' [xX] HEX_DIGIT+
 ;

/// bininteger     ::=  "0" ("b" | "B") bindigit+
BIN_INTEGER
 : '0' [bB] BIN_DIGIT+
 ;

/// floatnumber   ::=  pointfloat | exponentfloat
FLOAT_NUMBER
 : POINT_FLOAT
 | EXPONENT_FLOAT
 ;

/// imagnumber ::=  (floatnumber | intpart) ("j" | "J")
IMAG_NUMBER
 : ( FLOAT_NUMBER | INT_PART ) [jJ]
 ;

DOT : '.';
ELLIPSIS : '...';
STAR : '*';
OPEN_PAREN : '(' {opened++;};
CLOSE_PAREN : ')' {opened--;};
COMMA : ',';
COLON : ':';
SEMI_COLON : ';';
POWER : '**';
ASSIGN : '=';
OPEN_BRACK : '[' {opened++;};
CLOSE_BRACK : ']' {opened--;};
OR_OP : '|';
XOR : '^';
AND_OP : '&';
LEFT_SHIFT : '<<';
RIGHT_SHIFT : '>>';
ADD : '+';
MINUS : '-';
DIV : '/';
MOD : '%';
IDIV : '//';
NOT_OP : '~';
OPEN_BRACE : '{' {opened++;};
CLOSE_BRACE : '}' {opened--;};
LESS_THAN : '<';
GREATER_THAN : '>';
EQUALS : '==';
GT_EQ : '>=';
LT_EQ : '<=';
NOT_EQ_1 : '<>';
NOT_EQ_2 : '!=';
AT : '@';
ARROW : '->';
ADD_ASSIGN : '+=';
SUB_ASSIGN : '-=';
MULT_ASSIGN : '*=';
AT_ASSIGN : '@=';
DIV_ASSIGN : '/=';
MOD_ASSIGN : '%=';
AND_ASSIGN : '&=';
OR_ASSIGN : '|=';
XOR_ASSIGN : '^=';
LEFT_SHIFT_ASSIGN : '<<=';
RIGHT_SHIFT_ASSIGN : '>>=';
POWER_ASSIGN : '**=';
IDIV_ASSIGN : '//=';

SKIP_
 : ( SPACES | COMMENT | LINE_JOINING ) -> skip
 ;

UNKNOWN_CHAR
 : .
 ;

/* 
 * fragments 
 */

/// shortstring     ::=  "'" shortstringitem* "'" | '"' shortstringitem* '"'
/// shortstringitem ::=  shortstringchar | stringescapeseq
/// shortstringchar ::=  <any source character except "\" or newline or the quote>
fragment SHORT_STRING
 : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f'] )* '\''
 | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f"] )* '"'
 ;

/// longstring      ::=  "'''" longstringitem* "'''" | '"""' longstringitem* '"""'
fragment LONG_STRING
 : '\'\'\'' LONG_STRING_ITEM*? '\'\'\''
 | '"""' LONG_STRING_ITEM*? '"""'
 ;

/// longstringitem  ::=  longstringchar | stringescapeseq
fragment LONG_STRING_ITEM
 : LONG_STRING_CHAR
 | STRING_ESCAPE_SEQ
 ;

/// longstringchar  ::=  <any source character except "\">
fragment LONG_STRING_CHAR
 : ~'\\'
 ;

/// stringescapeseq ::=  "\" <any source character>
fragment STRING_ESCAPE_SEQ
 : '\\' .
 ;

/// nonzerodigit   ::=  "1"..."9"
fragment NON_ZERO_DIGIT
 : [1-9]
 ;

/// digit          ::=  "0"..."9"
fragment DIGIT
 : [0-9]
 ;

/// octdigit       ::=  "0"..."7"
fragment OCT_DIGIT
 : [0-7]
 ;

/// hexdigit       ::=  digit | "a"..."f" | "A"..."F"
fragment HEX_DIGIT
 : [0-9a-fA-F]
 ;

/// bindigit       ::=  "0" | "1"
fragment BIN_DIGIT
 : [01]
 ;

/// pointfloat    ::=  [intpart] fraction | intpart "."
fragment POINT_FLOAT
 : INT_PART? FRACTION
 | INT_PART '.'
 ;

/// exponentfloat ::=  (intpart | pointfloat) exponent
fragment EXPONENT_FLOAT
 : ( INT_PART | POINT_FLOAT ) EXPONENT
 ;

/// intpart       ::=  digit+
fragment INT_PART
 : DIGIT+
 ;

/// fraction      ::=  "." digit+
fragment FRACTION
 : '.' DIGIT+
 ;

/// exponent      ::=  ("e" | "E") ["+" | "-"] digit+
fragment EXPONENT
 : [eE] [+-]? DIGIT+
 ;

/// shortbytes     ::=  "'" shortbytesitem* "'" | '"' shortbytesitem* '"'
/// shortbytesitem ::=  shortbyteschar | bytesescapeseq
fragment SHORT_BYTES
 : '\'' ( SHORT_BYTES_CHAR_NO_SINGLE_QUOTE | BYTES_ESCAPE_SEQ )* '\''
 | '"' ( SHORT_BYTES_CHAR_NO_DOUBLE_QUOTE | BYTES_ESCAPE_SEQ )* '"'
 ;
    
/// longbytes      ::=  "'''" longbytesitem* "'''" | '"""' longbytesitem* '"""'
fragment LONG_BYTES
 : '\'\'\'' LONG_BYTES_ITEM*? '\'\'\''
 | '"""' LONG_BYTES_ITEM*? '"""'
 ;

/// longbytesitem  ::=  longbyteschar | bytesescapeseq
fragment LONG_BYTES_ITEM
 : LONG_BYTES_CHAR
 | BYTES_ESCAPE_SEQ
 ;

/// shortbyteschar ::=  <any ASCII character except "\" or newline or the quote>
fragment SHORT_BYTES_CHAR_NO_SINGLE_QUOTE
 : [\u0000-\u0009]
 | [\u000B-\u000C]
 | [\u000E-\u0026]
 | [\u0028-\u005B]
 | [\u005D-\u007F]
 ; 

fragment SHORT_BYTES_CHAR_NO_DOUBLE_QUOTE
 : [\u0000-\u0009]
 | [\u000B-\u000C]
 | [\u000E-\u0021]
 | [\u0023-\u005B]
 | [\u005D-\u007F]
 ; 

/// longbyteschar  ::=  <any ASCII character except "\">
fragment LONG_BYTES_CHAR
 : [\u0000-\u005B]
 | [\u005D-\u007F]
 ;

/// bytesescapeseq ::=  "\" <any ASCII character>
fragment BYTES_ESCAPE_SEQ
 : '\\' [\u0000-\u007F]
 ;

fragment SPACES
 : [ \t]+
 ;

fragment COMMENT
 : '#' ~[\r\n\f]*
 ;

fragment LINE_JOINING
 : '\\' SPACES? ( '\r'? '\n' | '\r' | '\f' )
 ;

fragment OTHER_ID_START
 : [\u2118\u212E\u309B\u309C]
 ;

/// id_start     ::=  <all characters in general categories Lu, Ll, Lt, Lm, Lo, Nl, the underscore, and characters with the Other_ID_Start property>
fragment ID_START
 : '_'
 | [\p{Letter}\p{Letter_Number}]
 | OTHER_ID_START
 ;

/// id_continue  ::=  <all characters in id_start, plus characters in the categories Mn, Mc, Nd, Pc and others with the Other_ID_Continue property>
fragment ID_CONTINUE
 : ID_START
 | [\p{Nonspacing_Mark}\p{Spacing_Mark}\p{Decimal_Number}\p{Connector_Punctuation}\p{Format}]
 ;
