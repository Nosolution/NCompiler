package lex;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Generate lex lexical parser in java language basing on given information
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class CodeGenerator {
    private final static String NPOS = "0";
    private PrintStream outStream;
    private String target;

    public CodeGenerator(String target) {
        this.target = target;
    }

    public CodeGenerator() {
        this("YyLex.java");
    }

    /**
     * Generate lexical parser code in java by given information
     *
     * @param tables     arrays converted by constructed dfa, whose size is assumed to be 4
     * @param actions    action list
     * @param startState the start state
     * @param rawDefs    user-specified codes outside the java class
     * @param localDefs  user-specified codes inside the yylex() method
     * @param userCode   user-specified codes outside the yylex() method
     */
    public void generate(List<int[]> tables, List<String> actions, int startState, List<String> rawDefs, List<String> localDefs, List<String> userCode) throws IOException {

        assert (tables.size() == 4);
        outStream = new PrintStream(new FileOutputStream(target));

        writeHeaders();
        for (String def : rawDefs) {
            outStream.println(def);
        }
        outStream.println();


        outStream.println("public class YyLex {");
        writeConstants(startState);
        writeFieldDecls();
        outStream.println("\tpublic int yylex() throws IOException {");
        writeInitialization();
        writeLocals(tables, localDefs);

        writeDriver(actions);
        outStream.println("\t\treturn yy_eof() ? SUCCESS : FAILURE;");
        outStream.println("\t}");

        //main entry
        outStream.println("\tpublic static void main(String[] args) throws IOException {");
        //TODO hint of input
        outStream.println("\t\tassert(args.length == 1);");
        outStream.println("\t\tYyLex lex = new YyLex(args[0]);");
        outStream.println("\t\tif (lex.yylex() == SUCCESS)");
        outStream.println("\t\t\tSystem.out.println(\"SUCCESS\");");
        outStream.println("\t\telse");
        outStream.println("\t\t\tSystem.out.println(\"FAILURE\");");
        outStream.println("\t}");
        outStream.println();

        writeConstructions();
        writeYyHelpers();
        outStream.println();

        for (String line : userCode) {
            outStream.print("\t");
            outStream.print(line);
        }

        outStream.println("}");
    }

    private void writeHeaders() {
        outStream.println("import java.io.*;");
    }

    private void writeConstants(int startState) {

        outStream.println("\tprivate final static int START_STATE = " + startState + ";");
        outStream.println("\tpublic final static int SUCCESS = 0;");
        outStream.println("\tpublic final static int FAILURE = -1;");
//            outStream.println("  private final static int YY_NOT_ACCEPT = -1;");
        outStream.println("\tprivate final static int YY_NO_STATE = -1;");
        outStream.println("\tprivate final static int YY_NO_ACTION = -1;");
        outStream.println("\tprivate final static int YY_NO_CHAR = -1;");
        outStream.println("\tprivate final static int YY_EOF = -1;");
    }

    private void writeFieldDecls() {
        outStream.println("\tprivate char[] yy_src = null;");
        outStream.println("\tprivate int yy_src_idx ;");
        outStream.println("\tprivate int yy_lookahead;");
        outStream.println("\tprivate int yy_matched_start;");
        outStream.println("\tprivate int yy_matched_end;");
        outStream.println("\tprivate boolean yy_in_process;");
        outStream.println("\tprivate int yy_line_cnt;");
        outStream.println("\tprivate int yy_char_cnt;");
        outStream.println("\tprivate PrintStream yy_out = null;");
        outStream.println("");
    }

    private void writeInitialization() {
        //member fields initialization
        outStream.println("\t\tassert(yy_src != null);");
        outStream.println("\t\tif(yy_out == null)");
        outStream.println("\t\t\tyy_set_printer();");
        outStream.println("\t\tyy_src_idx = 0;");
        outStream.println("\t\tyy_lookahead = 0;");
        outStream.println("\t\tyy_matched_start = " + NPOS + ";");
        outStream.println("\t\tyy_matched_end = " + NPOS + ";");
        outStream.println("\t\tyy_in_process = false;");
        outStream.println("\t\tyy_line_cnt = 0;");
        outStream.println("\t\tyy_char_cnt = 0;");
        outStream.println();
    }

    private void writeLocals(List<int[]> tables, List<String> localDefs) {
        writeTable("yy_charset", tables.get(0));
        writeTable("yy_base", tables.get(1));
        writeTable("yy_next", tables.get(2));
        writeTable("yy_accept", tables.get(3));
        outStream.println("\t\tint yy_cur_st = START_STATE;");
        outStream.println("\t\tint yy_last_accept_st = YY_NO_STATE;");
        outStream.println("\t\tint yy_next_st = YY_NO_STATE;");
        outStream.println("\t\tint yy_act = 0;");
        outStream.println("\t\tint yy_c_idx = YY_NO_CHAR;");
        for (String def : localDefs) {
            outStream.print("\t\t");
            outStream.println(def);
        }
        outStream.println();
    }

    private void writeDriver(List<String> actions) {
        outStream.println("\t\twhile (yy_lookahead != YY_EOF) {");

        //check if current state is an accept state
        outStream.println("\t\t\tif (yy_accept[yy_cur_st] != YY_NO_ACTION) {    ");
        outStream.println("\t\t\t\tyy_mark_end();");
        outStream.println("\t\t\t\tyy_last_accept_st = yy_cur_st;");
        outStream.println("\t\t\t}");
        outStream.println();

        //read a new char
        outStream.println("\t\t\tyy_lookahead = yy_advance();");
        //check if has reached the EOF
        outStream.println("\t\t\tif (yy_lookahead == YY_EOF){");
        outStream.println("\t\t\t\tif (yy_last_accept_st != YY_NO_STATE && yy_cur_st == yy_last_accept_st) {");
        outStream.println("\t\t\t\t\tyy_next_st = YY_NO_STATE;");
        outStream.println("\t\t\t\t} else {");
        outStream.println("\t\t\t\t\tSystem.err.println(\"Failed to parse source file\");");
        outStream.println("\t\t\t\t\treturn FAILURE;");
        outStream.println("\t\t\t\t}");
        outStream.println("\t\t\t} else {");
        outStream.println("\t\t\t\tyy_c_idx = yy_charset[yy_lookahead];");
        //if is not-supported char
        outStream.println("\t\t\t\tif (yy_c_idx == YY_NO_CHAR){");
        outStream.println("\t\t\t\t\tSystem.err.print(\"Not supported charset\");");
        outStream.println("\t\t\t\t\treturn FAILURE;");
        outStream.println("\t\t\t\t} else {");
        outStream.println("\t\t\t\t\tyy_next_st = yy_next[yy_base[yy_cur_st] + yy_c_idx];");
        outStream.println("\t\t\t\t}");
        outStream.println("\t\t\t}");
        outStream.println();

        //check next step
        //if no way to go, back to the string matched last time
        outStream.println("\t\t\tif (yy_next_st == YY_NO_STATE){");
        outStream.println("\t\t\t\tif (yy_last_accept_st != YY_NO_STATE) {");
        outStream.println("\t\t\t\t\tyy_cur_st = yy_last_accept_st;");
        outStream.println("\t\t\t\t\tyy_back_to_mark();");
        outStream.println();
        outStream.println("\t\t\t\t\tyy_act = yy_accept[yy_cur_st];");
        outStream.println("\t\t\t\t\tswitch(yy_act) {");

        //TODO ECHO, DISCARD, BEGIN, REJECT may need to be handled
        for (int i = 0; i < actions.size(); i++) {
            outStream.println("\t\t\t\t\t\tcase " + i + ": {");
            outStream.print("\t\t\t\t\t\t\t");
            String action = actions.get(i);
            if (action.equals("ECHO\n"))
                outStream.println("yy_echo();");
            else if (action.equals("DISCARD\n"))
                outStream.println("yy_discard();");
            else {
                String[] lines = actions.get(i).split("\n");
                for (int j = 0; j < lines.length; j++) {
                    outStream.println(lines[j]);
                    if (j < lines.length - 1)
                        outStream.print("\t\t\t\t\t\t\t");
                }
//                outStream.print(actions.get(i));
            }
            outStream.println("\t\t\t\t\t\t\tbreak;");
            outStream.println("\t\t\t\t\t\t}");
        }
        outStream.println("\t\t\t\t\t\tdefault:");
        outStream.println("\t\t\t\t\t\t\tbreak;");
        outStream.println("\t\t\t\t\t}");
        outStream.println();
        outStream.println("\t\t\t\t\tyy_cur_st = START_STATE;");
        outStream.println("\t\t\t\t\tyy_in_process = false;");
        outStream.println("\t\t\t\t\tyy_last_accept_st = YY_NO_STATE;");
        outStream.println("\t\t\t\t\tyy_mark_start();");
        outStream.println("\t\t\t\t}");
        //if no way to go and never matched
        outStream.println("\t\t\t\telse {");
        outStream.println("\t\t\t\t\tSystem.err.print(\"Failed to parse source file\");");
        outStream.println("\t\t\t\t\treturn FAILURE;");
        outStream.println("\t\t\t\t}");
        outStream.println("\t\t\t}");
        //iterate
        outStream.println("\t\t\telse {");
        outStream.println("\t\t\t\tyy_cur_st = yy_next_st;");
        outStream.println("\t\t\t\tyy_in_process = true;");
        outStream.println("\t\t\t}");


        outStream.println("\t\t}");
    }

    private void writeConstructions() {
        outStream.println("\tpublic YyLex(String path) throws IOException {");
        outStream.println("\t\tyy_src = readSourceFile(path);");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tpublic YyLex(InputStream input) throws IOException {");
        outStream.println("\t\tyy_src = readSource(input);");
        outStream.println("\t\t}");
        outStream.println();

        outStream.println("\tprivate static char[] readSource(InputStream input) throws IOException {");
        outStream.println("\t\tint size = 0;");
        outStream.println("\t\tint buffer_size = 1024;");
        outStream.println("\t\tbyte[] buffer = new byte[buffer_size];");
        outStream.println("\t\tint read_cnt = 0;");
        outStream.println("\t\twhile(true) {");
        outStream.println("\t\t\twhile(size < buffer_size && (read_cnt = input.read(buffer, size, buffer_size - size)) >= 0) {");
        outStream.println("\t\t\t\tsize += read_cnt;");
        outStream.println("\t\t\t}");
        outStream.println("\t\t\tif(size == buffer_size) {");
        outStream.println("\t\t\t\tbyte[] new_buffer = new byte[2*buffer_size];");
        outStream.println("\t\t\t\tSystem.arraycopy(buffer, 0, new_buffer, 0, buffer_size);");
        outStream.println("\t\t\t\tbuffer = new_buffer;");
        outStream.println("\t\t\t\tbuffer_size*=2;");
        outStream.println("\t\t\t} else");
        outStream.println("\t\t\t\tbreak;");
        outStream.println("\t\t}");
        outStream.println("\t\tinput.close();");
        outStream.println("\t\tbyte[] res = new byte[size];");
        outStream.println("\t\tSystem.arraycopy(buffer, 0, res, 0, size);");
        outStream.println("\t\treturn new String(res).toCharArray();");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate static char[] readSource(InputStream input, int size) throws IOException {");
        outStream.println("\t\tbyte[] buffer = new byte[size];");
        outStream.println("\t\tint read_cnt = 0;");
        outStream.println("\t\tint offset = 0;");
        outStream.println("\t\twhile(offset < size && (read_cnt = input.read(buffer, offset, size - offset)) >= 0) {");
        outStream.println("\t\t\toffset += read_cnt;");
        outStream.println("\t\t}");
        outStream.println("\t\tinput.close();");
        outStream.println("\t\treturn new String(buffer).toCharArray();");
        outStream.println("\t}");
        outStream.println();

        //read source file into array
        outStream.println("\tprivate static char[] readSourceFile(String filePath) throws IOException {");
        outStream.println("\t\tFile file = new File(filePath);");
        outStream.println("\t\tint size = (int)file.length();");
        outStream.println("\t\treturn readSource(new FileInputStream(file), size);");
        outStream.println("\t}");
        outStream.println();
    }

    private void writeYyHelpers() {
        outStream.println("\tprivate String yytext() {");
        outStream.println("\t\treturn new String(yy_src, yy_matched_start, yy_matched_end - yy_matched_start);");
        outStream.println("\t}");
        outStream.println("");

        outStream.println("\tprivate int yyleng() {");
        outStream.println("\t\treturn yy_matched_end - yy_matched_start;");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate void yy_echo() {");
        outStream.println("\t\tyy_out.print(yytext());");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate void yy_discard() {");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate boolean yy_eof() {");
        outStream.println("\t\t//Can be extended by user");
        outStream.println("\t\treturn true;");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate void yy_mark_start() {");
        outStream.println("\t\tfor(int i = yy_matched_start; i < yy_src_idx; i++) {");
        outStream.println("\t\t\tif(yy_src[i] == '\\n')");
        outStream.println("\t\t\t\tyy_line_cnt++;");
        outStream.println("\t\t\telse");
        outStream.println("\t\t\t\tyy_char_cnt++;");
        outStream.println("\t\t}");
        outStream.println("\t\tyy_matched_start = yy_src_idx;");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate void yy_mark_end() {");
        outStream.println("\t\tyy_matched_end = yy_src_idx;");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate void yy_back_to_mark() {");
        outStream.println("\t\tyy_src_idx = yy_matched_end;");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate int yy_advance() {");
        outStream.println("\t\tif(yy_src_idx < yy_src.length)");
        outStream.println("\t\t\treturn yy_src[yy_src_idx++];");
        outStream.println("\t\telse");
        outStream.println("\t\t\treturn YY_EOF;");
        outStream.println("\t}");
        outStream.println();

        outStream.println("\tprivate void yy_set_printer() throws IOException {");
        outStream.println("\t\tyy_out = new PrintStream(\"yy.out\");");
        outStream.println("\t}");
    }

    /**
     * Output int array
     *
     * @param name  specified array name
     * @param array array content
     */
    private void writeTable(String name, int[] array) {
        ///TODO  generate static table file instead of directly define tables in source file to prevent making source file too large, which will fail in compiling
        outStream.print("\t\tint[] " + name + " = new int[]{ ");
        for (int i = 0; i < array.length; i++) {
            outStream.print(array[i]);
            if (i < array.length - 1)
                outStream.print(", ");
        }
        outStream.println("};");
    }
}
