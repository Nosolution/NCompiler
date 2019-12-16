import java.io.*;
import java.util.List;

import java.util.Map;

import java.util.TreeMap;

//comment of multi-line raw definitions


public class YyLex {
	private final static int START_STATE = 2;
	public final static int SUCCESS = 0;
	public final static int FAILURE = -1;
	private final static int YY_NO_STATE = -1;
	private final static int YY_NO_ACTION = -1;
	private final static int YY_NO_CHAR = -1;
	private final static int YY_EOF = -1;
	private char[] yy_src = null;
	private int yy_src_idx ;
	private int yy_lookahead;
	private int yy_matched_start;
	private int yy_matched_end;
	private boolean yy_in_process;
	private int yy_line_cnt;
	private int yy_char_cnt;
	private PrintStream yy_out = null;

	public int yylex() throws IOException {
		assert(yy_src != null);
		if(yy_out == null)
			yy_set_printer();
		yy_src_idx = 0;
		yy_lookahead = 0;
		yy_matched_start = 0;
		yy_matched_end = 0;
		yy_in_process = false;
		yy_line_cnt = 0;
		yy_char_cnt = 0;

		int[] yy_charset = new int[]{ -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, -1, 2, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 5, 6, 7, -1, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, -1};
		int[] yy_base = new int[]{ 588, 1176, 196, 882, 294, 490, 0, 980, 1078, 392, 686, 98, 784};
		int[] yy_next = new int[]{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 3, -1, -1, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 5, 1, 1, 1, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 8, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 3, -1, -1, -1, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 11, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1};
		int[] yy_accept = new int[]{ 1, 4, -1, 2, -1, 4, 4, 4, -1, 3, 4, 4, 0};
		int yy_cur_st = START_STATE;
		int yy_last_accept_st = YY_NO_STATE;
		int yy_next_st = YY_NO_STATE;
		int yy_act = 0;
		int yy_c_idx = YY_NO_CHAR;

		while (yy_lookahead != YY_EOF) {
			if (yy_accept[yy_cur_st] != YY_NO_ACTION) {    
				yy_mark_end();
				yy_last_accept_st = yy_cur_st;
			}

			yy_lookahead = yy_advance();
			if (yy_lookahead == YY_EOF){
				if (yy_last_accept_st != YY_NO_STATE && yy_cur_st == yy_last_accept_st) {
					yy_next_st = YY_NO_STATE;
				} else {
					System.err.println("Failed to parse source file");
					return FAILURE;
				}
			} else {
				yy_c_idx = yy_charset[yy_lookahead];
				if (yy_c_idx == YY_NO_CHAR){
					System.err.print("Not supported charset");
					return FAILURE;
				} else {
					yy_next_st = yy_next[yy_base[yy_cur_st] + yy_c_idx];
				}
			}

			if (yy_next_st == YY_NO_STATE){
				if (yy_last_accept_st != YY_NO_STATE) {
					yy_cur_st = yy_last_accept_st;
					yy_back_to_mark();

					yy_act = yy_accept[yy_cur_st];
					switch(yy_act) {
						case 0: {
							yy_out.print("INT");
							break;
						}
						case 1: {
							yy_out.print("CHAR");
							break;
						}
						case 2: {
							yy_out.print(" ");
							break;
						}
						case 3: {
							yy_out.print("\n");
							break;
						}
						case 4: {
							yy_out.print("IDENTIFIER: " + yytext());
							//indent test
							break;
						}
						default:
							break;
					}

					yy_cur_st = START_STATE;
					yy_in_process = false;
					yy_last_accept_st = YY_NO_STATE;
					yy_mark_start();
				}
				else {
					System.err.print("Failed to parse source file");
					return FAILURE;
				}
			}
			else {
				yy_cur_st = yy_next_st;
				yy_in_process = true;
			}
		}
		return yy_eof() ? SUCCESS : FAILURE;
	}
	public static void main(String[] args) throws IOException {
		assert(args.length == 1);
		YyLex lex = new YyLex(args[0]);
		if (lex.yylex() == SUCCESS)
			System.out.println("SUCCESS");
		else
			System.out.println("FAILURE");
	}

	public YyLex(String path) throws IOException {
		yy_src = readSourceFile(path);
	}

	public YyLex(InputStream input) throws IOException {
		yy_src = readSource(input);
		}

	private static char[] readSource(InputStream input) throws IOException {
		int size = 0;
		int buffer_size = 1024;
		byte[] buffer = new byte[buffer_size];
		int read_cnt = 0;
		while(true) {
			while(size < buffer_size && (read_cnt = input.read(buffer, size, buffer_size - size)) >= 0) {
				size += read_cnt;
			}
			if(size == buffer_size) {
				byte[] new_buffer = new byte[2*buffer_size];
				System.arraycopy(buffer, 0, new_buffer, 0, buffer_size);
				buffer = new_buffer;
				buffer_size*=2;
			} else
				break;
		}
		input.close();
		byte[] res = new byte[size];
		System.arraycopy(buffer, 0, res, 0, size);
		return new String(res).toCharArray();
	}

	private static char[] readSource(InputStream input, int size) throws IOException {
		byte[] buffer = new byte[size];
		int read_cnt = 0;
		int offset = 0;
		while(offset < size && (read_cnt = input.read(buffer, offset, size - offset)) >= 0) {
			offset += read_cnt;
		}
		input.close();
		return new String(buffer).toCharArray();
	}

	private static char[] readSourceFile(String filePath) throws IOException {
		File file = new File(filePath);
		int size = (int)file.length();
		return readSource(new FileInputStream(file), size);
	}

	private String yytext() {
		return new String(yy_src, yy_matched_start, yy_matched_end - yy_matched_start);
	}

	private int yyleng() {
		return yy_matched_end - yy_matched_start;
	}

	private void yy_echo() {
		yy_out.print(yytext());
	}

	private void yy_discard() {
	}

	private boolean yy_eof() {
		//Can be extended by user
		return true;
	}

	private void yy_mark_start() {
		for(int i = yy_matched_start; i < yy_src_idx; i++) {
			if(yy_src[i] == '\n')
				yy_line_cnt++;
			else
				yy_char_cnt++;
		}
		yy_matched_start = yy_src_idx;
	}

	private void yy_mark_end() {
		yy_matched_end = yy_src_idx;
	}

	private void yy_back_to_mark() {
		yy_src_idx = yy_matched_end;
	}

	private int yy_advance() {
		if(yy_src_idx < yy_src.length)
			return yy_src[yy_src_idx++];
		else
			return YY_EOF;
	}

	private void yy_set_printer() throws IOException {
		yy_out = new PrintStream("yy.out");
	}

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
}
