grammar Python3;

tokens { INDENT, DEDENT }

@lexer::members {

   private java.util.LinkedList<Token> tokens = new java.util.LinkedList<>();

  private java.util.Stack<Integer> indents = new java.util.Stack<>();

  private int opened = 0;

  private Token lastToken = null;

  @Override
  public void emit(Token t) {
    super.setToken(t);
    tokens.offer(t);
  }

  @Override
  public Token nextToken() {

    if (_input.LA(1) == EOF && !this.indents.isEmpty()) {

      for (int i = tokens.size() - 1; i >= 0; i--) {
        if (tokens.get(i).getType() == EOF) {
          tokens.remove(i);
        }
      }

      this.emit(commonToken(Python3Parser.NEWLINE, "\n"));

      while (!indents.isEmpty()) {
        this.emit(createDedent());
        indents.pop();
      }

      this.emit(commonToken(Python3Parser.EOF, "<EOF>"));
    }

    Token next = super.nextToken();

    if (next.getChannel() == Token.DEFAULT_CHANNEL) {
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

  static int getIndentationCount(String spaces) {

    int count = 0;

    for (char ch : spaces.toCharArray()) {
      switch (ch) {
        case '\t':
          count += 8 - (count % 8);
          break;
        default:
          count++;
      }
    }

    return count;
  }

  boolean atStartOfInput() {
    return super.getCharPositionInLine() == 0 && super.getLine() == 1;
  }
}

single_input
 : NEWLINE
 | simple_stmt
 | compound_stmt NEWLINE
 ;

file_input
 : ( NEWLINE | stmt )* EOF
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
 : testlist_star_expr ( augassign ( testlist)
                      | ( '=' ( testlist_star_expr ) )*
                      )
 ;           

testlist_star_expr
 : ( test | star_expr ) ( ',' ( test |  star_expr ) )* ','?
 ;


augassign
 : '+=' 
 | '-=' 
 | '*='
 | '/='
 ;

flow_stmt
 : break_stmt 
 | continue_stmt 
 | return_stmt 
 | raise_stmt
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

raise_test
 : test
 ;

raise_stmt
 : RAISE ( raise_test ( FROM raise_test )? )?
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
 ;

if_suite
 : suite
 ;

elif_suite
 : suite
 ;

else_suite
 : suite
 ;

elif_test
 : test
 ;

if_test
 : test
 ;

if_stmt
 : IF if_test (operators if_test)* ':' if_suite ( ELIF elif_test ':' elif_suite )* ( ELSE ':' else_suite )?
 ;


while_test
 : test
 ;

while_suite
 : suite
 ;

while_stmt
 : WHILE while_test ':' while_suite
 ;

for_stmt
 : FOR exprlist IN testlist ':' suite
 ;

try_stmt
 : TRY ':' try_suite ( ( except_clause ':' except_clause_suite )+
                   ( ELSE ':' try_else_suite )?
                   ( FINALLY ':' finally_suite )?
                 | FINALLY ':' finally_suite
                 )
 ;

except_clause
 : EXCEPT ( test ( AS NAME )? )?
 ;

suite
 : simple_stmt 
 | NEWLINE INDENT stmt+ DEDENT
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

test
 : comparison
 ;

comparison
 : expr ( comp_op expr )*
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
 | AND
 | OR
 ;


operators
 : and_op
 | or_op
 ;

and_op
 : AND
 ;

or_op
 : OR
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
 : '(' ( testlist_comp )? ')'
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
 | '[' subscriptlist ']' 
 | '.' NAME
 ;

subscriptlist
 : subscript ( ',' subscript )* ','?
 ;

subscript
 : test 
 | test? ':' test? sliceop?
 ;

sliceop
 : ':' test?
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

argument
 : test comp_for? 
 | test '=' test
 ;

comp_iter
 : comp_for 
 | comp_if
 ;

comp_for
 : FOR exprlist IN test comp_iter?
 ;

comp_if
 : IF comparison comp_iter?
 ;

str
 : STRING_LITERAL
 | BYTES_LITERAL
 ;

number
 : integer
 ;

integer
 : DECIMAL_INTEGER
 | HEX_INTEGER
 ;

/*
 * lexer
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

STRING_LITERAL
 : [uU]? [rR]? ( SHORT_STRING | LONG_STRING )
 ;

DECIMAL_INTEGER
 : NON_ZERO_DIGIT DIGIT*
 | '0'+
 ;

HEX_INTEGER
 : '0' [xX] HEX_DIGIT+
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

SKIP_
 : ( SPACES | COMMENT | LINE_JOINING ) -> skip
 ;

UNKNOWN_CHAR
 : .
 ;

/* 
 * fragments 
 */

fragment SHORT_STRING
 : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f'] )* '\''
 | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f"] )* '"'
 ;

fragment LONG_STRING
 : '\'\'\'' LONG_STRING_ITEM*? '\'\'\''
 | '"""' LONG_STRING_ITEM*? '"""'
 ;

fragment LONG_STRING_ITEM
 : LONG_STRING_CHAR
 | STRING_ESCAPE_SEQ
 ;

fragment LONG_STRING_CHAR
 : ~'\\'
 ;

fragment STRING_ESCAPE_SEQ
 : '\\' .
 ;

fragment NON_ZERO_DIGIT
 : [1-9]
 ;

fragment DIGIT
 : [0-9]
 ;

fragment HEX_DIGIT
 : [0-9a-fA-F]
 ;

fragment SHORT_BYTES
 : '\'' ( SHORT_BYTES_CHAR_NO_SINGLE_QUOTE | BYTES_ESCAPE_SEQ )* '\''
 | '"' ( SHORT_BYTES_CHAR_NO_DOUBLE_QUOTE | BYTES_ESCAPE_SEQ )* '"'
 ;

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

fragment LONG_BYTES_CHAR
 : [\u0000-\u005B]
 | [\u005D-\u007F]
 ;

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

fragment ID_START
 : '_'
 | [\p{Letter}\p{Letter_Number}]
 | OTHER_ID_START
 ;

fragment ID_CONTINUE
 : ID_START
 | [\p{Nonspacing_Mark}\p{Spacing_Mark}\p{Decimal_Number}\p{Connector_Punctuation}\p{Format}]
 ;