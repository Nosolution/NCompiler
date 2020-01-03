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
{W}+	yy_out.print(" ");
\r?\n	yy_out.print("\n");
{L}({L}|{D})*		{ 
	 yy_out.print("IDENTIFIER: " + yytext());
	 //indent test
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


