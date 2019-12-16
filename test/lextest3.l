	import java.util.List;
%{
import java.util.Map;
import java.util.TreeMap;
//comment of multi-line raw definitions
%}
D			[0-9]
L			[a-zA-Z_]
H			[a-fA-F0-9]
E			([Ee][+-]?{D}+)
P           ([Pp][+-]?{D}+)
W 			[ \t]
FS			(f|F|l|L)
%%
	int a = 0;
	int b = 0;
	//local declaration
"/*"			{
	 comment();
	 yy_out.print("MULTI_LINE_COMMENT");
	}
"//"[^\n]*      {
	 yy_out.print("SINGLE_LINE_COMMENT");
	}




"boolean" 	yy_out.print("BOOLEAN"); 
"int" 	yy_out.print("INT"); 
"char" 	yy_out.print("CHAR");
"byte" 	yy_out.print("BYTE");
"short"	yy_out.print("SHORT"); 
"long"	yy_out.print("LONG"); 
"float"	yy_out.print("FLOAT"); 
"double" yy_out.print("DOUBLE");
"enum"	yy_out.print("ENUM"); 
"String" yy_out.print("STRINGTYPE");
"List" yy_out.print("LIST");
"Map" yy_out.print("MAP");
"Set" yy_out.print("Set");

"for"	{
	yy_out.print("FOR"); 
	}
"break"			{ 
	 yy_out.print("BREAK"); 
	}
"continue"		{ 
	 yy_out.print("CONTINUE"); 
	}
"while"			{ 
	 yy_out.print("WHILE"); 
	}
"do"			{ 
	 yy_out.print("DO"); 
	}
"if"			{ 
	 yy_out.print("IF"); 
	}
"else"			{ 
	 yy_out.print("ELSE"); 
	}
"switch"		{ 
	 yy_out.print("SWITCH"); 
	}
"case"			{ 
	 yy_out.print("CASE"); 
	}
"default"		{ 
	 yy_out.print("DEFAULT"); 
	}
	
"try" yy_out.print("TRY");
"catch" yy_out.print("CATCH");
"throw" yy_out.print("THROW");
"throws" yy_out.print("THROWS");
"import" yy_out.print("IMPORT");
"this" yy_out.print("THIS");
"new" yy_out.print("NEW");

"final"			{ 
	 yy_out.print("FINAL"); 
	}
"static"		{ 
	 yy_out.print("STATIC"); 
	}
"void"			{ 
	 yy_out.print("VOID"); 
	}
"return"		{ 
	 yy_out.print("RETURN"); 
	}
"public" yy_out.print("PUBLIC");
"protected" yy_out.print("PROTECTED");
"private" yy_out.print("PRIVATE");



"..."			{ 
	 yy_out.print("ELLIPSIS"); 
	}
">>="			{ 
	 yy_out.print("RIGHT_ASSIGN"); 
	}
"<<="			{ 
	 yy_out.print("LEFT_ASSIGN"); 
	}
"+="			{ 
	 yy_out.print("ADD_ASSIGN"); 
	}
"-="			{ 
	 yy_out.print("SUB_ASSIGN"); 
	}
"*="			{ 
	 yy_out.print("MUL_ASSIGN"); 
	}
"/="			{ 
	 yy_out.print("DIV_ASSIGN"); 
	}
"%="			{ 
	 yy_out.print("MOD_ASSIGN"); 
	}
"&="			{ 
	 yy_out.print("AND_ASSIGN"); 
	}
"^="			{ 
	 yy_out.print("XOR_ASSIGN"); 
	}
"|="			{ 
	 yy_out.print("OR_ASSIGN"); 
	}
">>"			{ 
	 yy_out.print("RIGHT_OP"); 
	}
"<<"			{ 
	 yy_out.print("LEFT_OP"); 
	}
"++"			{ 
	 yy_out.print("INC_OP"); 
	}
"--"			{ 
	 yy_out.print("DEC_OP"); 
	}
"->"			{ 
	 yy_out.print("PTR_OP"); 
	}
"&&"			{ 
	 yy_out.print("AND_OP"); 
	}
"||"			{ 
	 yy_out.print("OR_OP"); 
	}
"<="			{ 
	 yy_out.print("LE_OP"); 
	}
">="			{ 
	 yy_out.print("GE_OP"); 
	}
"=="			{ 
	 yy_out.print("EQ_OP"); 
	}
"!="			{ 
	 yy_out.print("NE_OP"); 
	}
";"			{ 
	yy_out.print(";"); 
	}
"{"		{ 
	 yy_out.print("{"); 
	}
"}"		{ 
	 yy_out.print("}"); 
	}
","			{ 
	 yy_out.print(","); 
	}
":"			{ 
	 yy_out.print(":"); 
	}
"="			{ 
	 yy_out.print("="); 
	}
"("			{ 
	 yy_out.print("("); 
	}
")"			{ 
	 yy_out.print(")"); 
	}
"["		{ 
	 yy_out.print("["); 
	}
"]"		{ 
	 yy_out.print("]"); 
	}
"."			{ 
	 yy_out.print("."); 
	}
"&"			{ 
	 yy_out.print("&"); 
	}
"!"			{ 
	 yy_out.print("!"); 
	}
"~"			{ 
	 yy_out.print("~"); 
	}
"-"			{ 
	 yy_out.print("-"); 
	}
"+"			{ 
	 yy_out.print("+"); 
	}
"*"			{ 
	 yy_out.print("*"); 
	}
"/"			{ 
	 yy_out.print("/"); 
	}
"%"			{ 
	 yy_out.print("%"); 
	}
"<"			{ 
	yy_out.print("<"); 
	}
">"			{ 
	 yy_out.print(">"); 
	}
"^"			{ 
	 yy_out.print("^"); 
	}
"|"			{ 
	 yy_out.print("||"); 
	}
"?"			{ 
	 yy_out.print("?"); 
	}
{W}+	yy_out.print(" ");
{W}*\n  yy_out.print("\n");

{L}({L}|{D})*		{ 
	 yy_out.print("IDENTIFIER: " + yytext()); 
	}
0[xX]{H}+{FS}?		{ 
	 yy_out.print("HEX_INTEGER"); 
	}
0[0-7]*{FS}?		{ 
	 yy_out.print("OCT_INTEGER"); 
	}
[1-9]{D}*{FS}?		{ 
	 yy_out.print("DECIMAL_INTEGER: " + yytext()); 
	}
'([^\\'\n]|[\\\0\n\t\f])' { 
	 yy_out.print("CHAR_LITERAL: " + yytext()); 
	}
\"[^\\"\n]|\\([\\\0\n\t\f])*\"	{ 
	 yy_out.print("STR_LITERAL: " + yytext()); 
	}
{D}+{E}{FS}?		{ 
	 yy_out.print("EXP_NUM"); 
	}
{D}+"."{D}*{E}?{FS}?	{ 
	 yy_out.print("FLOAT_NUM"); 
	}
0[xX]{H}+{P}{FS}?	{ 
	 yy_out.print("HEX_PNUM"); 
	}
0[xX]{H}*"."{H}+{P}{FS}?     { 
	 yy_out.print("HEX_FLOAT_PNUM"); 
	}
0[xX]{H}+"."{H}*{P}{FS}?     {
	yy_out.print("HEX_FLOAT_PNUM"); 
	}
.			{
	yy_out.print("ERROR!");
	System.err.println("ERROR!");
}

%%
private void comment(){
	int c, prev = 0;
	while((yy_lookahead = yy_advance())!=YY_EOF){
		c = yy_lookahead;
		if(c == '/' && prev == '*')
			return;
		prev = c ;
	}
	System.err.println("ERROR: unterminated comment!");
}


