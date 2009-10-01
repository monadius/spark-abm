package parser;

%%
%function get_token
%type Symbol
%eofval{
	return sf.newSymbol("EOF", <CUPSYM>.EOF, -1, -1);
%eofval}
%eofclose

%line
%column

%class Scanner
%{
  	StringBuffer string = new StringBuffer();
	public Scanner(java.io.InputStream r, SymbolFactory sf){
		this(r);
		this.sf = sf;
	}
	
	private SymbolFactory sf;
	
	private Symbol lastSymbol;
	
	public Symbol peekToken() throws java.io.IOException {
		if (lastSymbol != null)
			return lastSymbol;
		else
			return lastSymbol = get_token();
	}
	
	public Symbol nextToken() throws java.io.IOException {
		if (lastSymbol != null) {
			Symbol tmp = lastSymbol;
			lastSymbol = null;
			return tmp;
		}
		
		return get_token();
	}
%}
%eofval{
    return sf.newSymbol("EOF", sym.EOF, -1, -1);
%eofval}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f]

Comment = ";" {InputCharacter}* {LineTerminator}?
IdentifierSymbol = [a-zA-Z]
OperatorSymbol = [?=*!<>+/%\^&-]

Identifier = {IdentifierSymbol} ({IdentifierSymbol} | [_0-9-])*
Operator = {OperatorSymbol}+

DoubleLiteral = [-]? ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

StringCharacter = [^\r\n\"\\]

EndOfCode = "@#$#@#$#@" | "***end***"

%state STRING

%%

<YYINITIAL> {
	{EndOfCode} { return sf.newSymbol("EOF", sym.EOF, yyline, yycolumn); }

	/* keywords */
	"for" { return sf.newSymbol("for", sym.FOR, yyline, yycolumn); }
	"global" { return sf.newSymbol("global", sym.GLOBAL, yyline, yycolumn); }
	"shared" { return sf.newSymbol("shared", sym.SHARED, yyline, yycolumn); }
	"var" { return sf.newSymbol("var", sym.VAR, yyline, yycolumn); }
	"to" { return sf.newSymbol("to", sym.TO, yyline, yycolumn); }
	"end" { return sf.newSymbol("end", sym.END, yyline, yycolumn); }
	"class" { return sf.newSymbol("class", sym.CLASS, yyline, yycolumn); }
	"agent" { return sf.newSymbol("agent", sym.AGENT, yyline, yycolumn); }
	"model" { return sf.newSymbol("model", sym.MODEL, yyline, yycolumn); }
	"space" { return sf.newSymbol("space", sym.SPACE, yyline, yycolumn); }
	"@" { return sf.newSymbol("@", sym.ANNOTATION, yyline, yycolumn); }
	
  	/* separators */
	"(" { return sf.newSymbol("LPAREN", sym.LPAREN, yyline, yycolumn); }
	")" { return sf.newSymbol("RPAREN", sym.RPAREN, yyline, yycolumn); }
	"[" { return sf.newSymbol("LBRACK", sym.LBRACK, yyline, yycolumn); }
	"]" { return sf.newSymbol("RBRACK", sym.RBRACK, yyline, yycolumn); }
	"{" { return sf.newSymbol("LBRACE", sym.LBRACE, yyline, yycolumn); }
	"}" { return sf.newSymbol("RBRACE", sym.RBRACE, yyline, yycolumn); }

	/* operators */
	":" { return sf.newSymbol("COLON", sym.COLON, yyline, yycolumn); }
	"=" { return sf.newSymbol("EQ", sym.EQ, yyline, yycolumn); }
	"." { return sf.newSymbol("DOT", sym.DOT, yyline, yycolumn); }
	"," { return sf.newSymbol("COMMA", sym.COMMA, yyline, yycolumn); }

  	/* string literal */
  	\"    { yybegin(STRING); string.setLength(0); }
  
  	/* double literal */
  	{DoubleLiteral} { return sf.newSymbol("DOUBLE", sym.DOUBLE, new Double(yytext()), yyline, yycolumn); }

	{Comment}	{}
	{WhiteSpace}	{}
	{LineTerminator}	{}
	
	{Identifier} { return sf.newSymbol("IDENTIFIER", sym.IDENTIFIER, yytext(), yyline, yycolumn); }
	{Operator} { return sf.newSymbol("OPERATOR", sym.OPERATOR, yytext(), yyline, yycolumn); }
}

<STRING> {
  	\"  { yybegin(YYINITIAL); return sf.newSymbol("STRING", sym.STRING, string.toString(), yyline, yycolumn); }

  {StringCharacter}+             { string.append( yytext() ); }

  /* escape sequences */
  "\\b"                          { string.append( '\b' ); }
  "\\t"                          { string.append( '\t' ); }
  "\\n"                          { string.append( '\n' ); }
  "\\f"                          { string.append( '\f' ); }
  "\\r"                          { string.append( '\r' ); }
  "\\\""                         { string.append( '\"' ); }
  "\\'"                          { string.append( '\'' ); }
  "\\\\"                         { string.append( '\\' ); }
}

. { System.err.println("Illegal character: "+yytext()); }