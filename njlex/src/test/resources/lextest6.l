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
"int" 	yy_out.print("INT"); 
"char" 	yy_out.print("CHAR");
"public" yy_out.print("PUBLIC");
"void" yy_out.print("VOID");
"return" yy_out.print("RETURN");

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
"<"			{ 
	yy_out.print("<"); 
	}
">"			{ 
	 yy_out.print(">"); 
	}
{W}+	yy_out.print(" ");
\r?\n	yy_out.print("\n");
{L}({L}|{D})*		{ 
	 yy_out.print("IDENTIFIER: " + yytext());
	 //indent test
	}
[1-9]{D}*{FS}?		{ 
	 yy_out.print("DECIMAL_INTEGER: " + yytext()); 


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


