package yacc;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Lex simulator
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/21
 */
public class YyLex {

    private int yy_lookahead;
    private int yy_idx;
    private char[] yy_input;
    public Object yylval;
    private final static int EOF = -1;
    private final static int NUM = 2;
    private final static int TERMINAL = 0;


    public int yylex() {
        do {
            yy_advance();
        }
        while (yy_lookahead != EOF && yy_lookahead != '\n' && (yy_lookahead == ' ' || yy_lookahead == '\t'));

        if (yy_lookahead == EOF || yy_lookahead == '\n')
            return TERMINAL;
        else {
            char c = (char) yy_lookahead;
            if (Character.isDigit(c) || c == '.') {
                boolean dot = c == '.';
                StringBuilder number = new StringBuilder();
                number.append(c);
                yy_advance();
                c = (char) yy_lookahead;
//                do {
                while (yy_lookahead != EOF && (Character.isDigit(c) || !dot && c == '.')) {
                    number.append(c);
                    yy_advance();
                    if (c == '.')
                        dot = !dot;
                    c = (char) yy_lookahead;
                }
//                } while (dot);
                yylval = Double.parseDouble(number.toString());
                backward();
                return NUM;
            } else
                return c;
        }
    }

    private void yy_advance() {
        yy_lookahead = yy_idx == yy_input.length ? EOF : yy_input[yy_idx++];
    }

    private void backward() {
        if (yy_idx > 0) {
            if (yy_lookahead == EOF)
                yy_lookahead = yy_input[yy_idx - 1];
            else
                yy_lookahead = yy_input[--yy_idx];
        }
    }

    public YyLex(String path) throws IOException {
        yy_input = readSourceFile(path);
        yy_idx = 0;
        yy_lookahead = ' ';
        yylval = null;
    }

    private static char[] readSource(InputStream input, int size) throws IOException {
        byte[] buffer = new byte[size];
        int read_cnt = 0;
        int offset = 0;
        while (offset < size && (read_cnt = input.read(buffer, offset, size - offset)) >= 0) {
            offset += read_cnt;
        }
        input.close();
        return new String(buffer).toCharArray();
    }

    private static char[] readSourceFile(String filePath) throws IOException {
        File file = new File(filePath);
        int size = (int) file.length();
        return readSource(new FileInputStream(file), size);
    }

    public Object getYylval() {
        return yylval;
    }

    String getSource() {
        return new String(yy_input);
    }


}
