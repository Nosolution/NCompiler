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


"int" 	yy_out.print("INT"); 
"char" 	yy_out.print("CHAR");

.			{
	yy_out.print("ERROR!");
	System.err.println("ERROR!");
}

%%
private void comment(){
	int c, prev = 0;
	while((yy_lookhead = yy_advance())!=YY_EOF){
		c = yy_lookhead;
		if(c == '/' && prev == '*')
			return;
		prev = c ;
	}
	System.err.println("ERROR: unterminated comment!");
}


