grammar FOOL;

@lexer::members {
public int lexicalErrors=0;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

/*
    Partendo dall'inizio si ha un programma prog che è definito da un
    body cioè il progbody.
*/
prog : progbody EOF ;

/*
    il progbody ha sempre almeno una dichiarazione dec (+ significa
    chiusura positiva, almeno una dec) e poi exp è il vero e proprio
    corpo del programma.

    exp SEMIC è il vecchio caso senza dichiarazioni.
*/
progbody : LET ( cldec+ dec* | dec+ ) IN exp SEMIC #letInProg
         | exp SEMIC                               #noDecProg
         ;

cldec  : CLASS ID
              LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
              CLPAR
                   methdec*
              CRPAR ;

methdec : FUN ID COLON type
              LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
                   (LET dec+ IN)? exp
              SEMIC ;

/* Questa è la dichiarazione DEC
    puo essere dichiarazione di variabile o di funzione.
    var e fun sono due nuovi lessemi, VAR e FUN i token.
    ASS è l'assegnamento ad esempio int x = 5*9 devo fare
    var x : int = 5*9 ; (SEMIC)

    La dichiarazione di funzione assomiglia a quella di variabile
    fun g:bool (b:bool)
            let
                (corpo della funzione)
            in

    ricordo il funzionamento di let in
    let
        int x = 5;
        int y = 9;

    in
        codice senza dichiarazioni
        potrei avere altre let in annidate

    In poche parole dentro a "let" si mettono le dichiarazioni di variabili e
    funzioni, invece dentro a "in" ci va il main.
*/
dec : VAR ID COLON type ASS exp SEMIC #vardec
    | FUN ID COLON type
          LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
               (LET dec+ IN)? exp
          SEMIC #fundec
    ;

/*
    Se compare ID è una variabile altrimenti se compare
    ID LPAR(exp (COMMA exp)*) ? RPAR è una chiamata di funzione
    dove il ? è l'argomento eventualmente vuoto.
    Bisogna fare questo trick perchè la virgola comma esiste solo nel
    caso di due argomenti.
   ==========================================
   il cancelletto #name sono i nomi che do alle produzioni e mi serviranno
   in futuro quando visito l'albero sintattico.
*/
exp     : exp (TIMES | DIV) exp #timesDiv
        | exp (PLUS | MINUS) exp #plusMinus
        | exp (EQ | GE | LE) exp #comp
        | exp (AND | OR) exp #andOr
	    | NOT exp #not
        | LPAR exp RPAR #pars
    	| MINUS? NUM #integer
	    | TRUE #true
	    | FALSE #false
	    | NULL #null
	    | NEW ID LPAR (exp (COMMA exp)* )? RPAR #new
	    | IF exp THEN CLPAR exp CRPAR ELSE CLPAR exp CRPAR #if
	    | PRINT LPAR exp RPAR #print
        | ID #id
	    | ID LPAR (exp (COMMA exp)* )? RPAR #call
	    | ID DOT ID LPAR (exp (COMMA exp)* )? RPAR #dotCall
        ;


type    : INT #intType
        | BOOL #boolType
 	    | ID #idType
 	    ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

PLUS  	: '+' ;
MINUS   : '-' ;     // ok
TIMES   : '*' ;
DIV 	: '/' ;     // ok
LPAR	: '(' ;
RPAR	: ')' ;
CLPAR	: '{' ;
CRPAR	: '}' ;
SEMIC 	: ';' ;
COLON   : ':' ;
COMMA	: ',' ;
DOT	    : '.' ;     // ok
OR	    : '||';     // ok
AND	    : '&&';     // ok
NOT	    : '!' ;     // ok
GE	    : '>=' ;    // ok
LE	    : '<=' ;    // ok
EQ	    : '==' ;
ASS	    : '=' ;
TRUE	: 'true' ;
FALSE	: 'false' ;
IF	    : 'if' ;
THEN	: 'then';
ELSE	: 'else' ;
PRINT	: 'print' ;
LET     : 'let' ;
IN      : 'in' ;
VAR     : 'var' ;
FUN	    : 'fun' ;
CLASS	: 'class' ;     // ok
NEW 	: 'new' ;       // ok
NULL    : 'null' ;      // ok
INT	    : 'int' ;
BOOL	: 'bool' ;
NUM     : '0' | ('1'..'9')('0'..'9')* ;

/* ID è un token con la relativa l'espressione regolare con i lessemi
    che metcha, cioè una lettera maiuscola o minuscola seguita da una
    lettera maiuscola /minuscola / un numero.
*/
ID  	: ('a'..'z'|'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')* ;


WHITESP  : ( '\t' | ' ' | '\r' | '\n' )+    -> channel(HIDDEN) ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;

ERR   	 : . { System.out.println("Invalid char: "+ getText() +" at line "+getLine()); lexicalErrors++; } -> channel(HIDDEN);


