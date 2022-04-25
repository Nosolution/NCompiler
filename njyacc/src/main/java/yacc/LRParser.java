package yacc;

import yacc.entity.Token;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * LRParser
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/21
 */
public class LRParser {


    private final static List<Character> symbols = new ArrayList<>(Arrays.asList('+', '-', '*', '/', '^', '(', ')'));
    //number of all supported tokens, including heads of productions and chars. production name are arranged ahead
    private final static int yy_token_num = 10;
    private final static int yy_state_num = 34;
    //number of heads, which means its value need to be calculated
    private final static int yy_head_num = 3;
    //number of productions
    private final static int yy_prod_num = 9;
    //information of all production,2*i is production name, 2*i+1 is its length
    private final static int[] yy_prod = new int[]{0, 0, 0, 0, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 2, 1, 3, 1, 1};
    //Each state' s base offset of yy_next
    private final static int[] yy_base = new int[34];
    //LR(1) table indicating the next step. Positive numbers is shift actions while negative ones are reduce actions
    //value = [base:token_order]
    private final static int[] yy_next = new int[]{
            0, 1, 4, 0, 2, 0, 0, 0, 3, 0,
            -1, 0, 0, 5, 6, 7, 8, 9, 0, 0,
            0, 10, 4, 0, 2, 0, 0, 0, 3, 0,
            0, 11, 14, 0, 12, 0, 0, 0, 13, 0,
            -9, 0, 0, -9, -9, -9, -9, -9, 0, -9,
            0, 15, 4, 0, 2, 0, 0, 0, 3, 0,
            0, 16, 4, 0, 2, 0, 0, 0, 3, 0,
            0, 17, 4, 0, 2, 0, 0, 0, 3, 0,
            0, 18, 4, 0, 2, 0, 0, 0, 3, 0,
            0, 19, 4, 0, 2, 0, 0, 0, 3, 0,
            -7, 0, 0, -7, -7, -7, -7, 9, 0, -7,
            0, 0, 0, 21, 22, 23, 24, 25, 0, 20,
            0, 33, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 26, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 0, 0, -9, -9, -9, -9, -9, 0, -9,
            -2, 0, 0, -2, -2, 7, 8, 9, 0, -2,
            -3, 0, 0, -3, -3, 7, 8, 9, 0, -3,
            -4, 0, 0, -4, -4, -4, -4, 9, 0, -4,
            -5, 0, 0, -5, -5, -5, -5, 9, 0, -5,
            -6, 0, 0, -6, -6, -6, -6, 9, 0, -6,
            -8, 0, 0, -8, -8, -8, -8, -8, 0, -8,
            0, 27, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 28, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 29, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 30, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 31, 14, 0, 12, 0, 0, 0, 13, 0,
            0, 0, 0, 21, 22, 23, 24, 25, 0, 32,
            0, 0, 0, -2, -2, 23, 24, 25, 0, -2,
            0, 0, 0, -3, -3, 23, 24, 25, 0, -3,
            0, 0, 0, -4, -4, -4, -4, 25, 0, -4,
            0, 0, 0, -5, -5, -5, -5, 25, 0, -5,
            0, 0, 0, -6, -6, -6, -6, 25, 0, -6,
            0, 0, 0, -8, -8, -8, -8, -8, 0, -8,
            0, 0, 0, -7, -7, -7, -7, 25, 0, -7
    };
    private final static String[] yy_productions = new String[]{"", "E'->E", "E->E+E", "E->E-E", "E->E*E", "E->E/E", "E->E^E", "E->-E", "E->(E)", "E->NUM"};
    //map char to token number
    private int[] yy_token_map;
    private final static int E = 1;
    private final static int NUM = 2;
    private final static int YY_TERMINAL_TOKEN = 0;
    private final static int YY_INIT_STATE = 0;
    private final static int SUCCESS = 1;
    private final static int FAILURE = 0;

    private final static String TARGET = "yy.output";
    private YyLex yylex;
    private PrintStream yy_out;
    private int yy_lookahead;
    private int yy_transfer;
    private Stack<Token> yy_token_stack;
    private Stack<Integer> yy_state_stack;


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR: wrong command");
            System.out.println("Please input: java LRParser <source path>");
        } else {
            LRParser parser = new LRParser();
            int res = parser.parser(args[0]);
            System.out.println(res == SUCCESS ? "SUCCESS" : "FAILURE");
        }
    }


    public LRParser() {
        yy_token_map = new int[256];
        yylex = null;
        yy_out = null;
        yy_lookahead = 0;
        yy_transfer = 0;
        yy_token_stack = new Stack<>();
        yy_state_stack = new Stack<>();
    }

    public int parser(String path) {

        try {
            yylex = new YyLex(path);
        } catch (IOException e) {
            e.printStackTrace();
            return FAILURE;
        }

        try {
            yy_set_printer();
        } catch (IOException e) {
            e.printStackTrace();
            return FAILURE;
        }

        yy_out.println("Source: " + yylex.getSource());


        Token yy_cur_tk;
        List<Token> yy_prod_body = new ArrayList<>();

        Arrays.fill(yy_token_map, 0);
        yy_token_map[YY_TERMINAL_TOKEN] = 0;
        yy_token_map[E] = 1;
        yy_token_map[NUM] = 2;
        int number = 3;
        for (char symbol : symbols) {
            yy_token_map[symbol] = number++;
        }

        for (int i = 0; i < yy_base.length; i++) {
            yy_base[i] = i * yy_token_num;
        }

        //initialization
        yy_state_stack.push(YY_INIT_STATE);
        yy_token_stack.push(new Token(YY_TERMINAL_TOKEN, null));
        yy_next_look();

        PARSING:
        while (true) {
            //if to shift
            if (yy_transfer > 0) {
                //record state transitions
                yy_state_stack.push(yy_transfer);
                Token t = new Token();
                t.setName(yy_lookahead);
                //if the token's value need to be stored
                if (yy_lookahead < yy_head_num)
                    t.setVal(yylval());
                //Record encountered tokens
                yy_token_stack.push(t);
                //update next char
                yy_next_look();
            }
            //else if to reduce
            else if (yy_transfer < 0) {
                int yy_prod_idx = -yy_transfer;
                yy_cur_tk = new Token(yy_prod[2 * yy_prod_idx], null);
                int body_cnt = yy_prod[2 * yy_prod_idx + 1];
                yy_prod_body.clear();
                for (int i = 0; i < body_cnt; i++)
                    yy_prod_body.add(yy_token_stack.pop());
                if (body_cnt > 1)
                    //reverse order
                    Collections.reverse(yy_prod_body);
                switch (yy_prod_idx) {
                    //E'->E
                    case 1: {
                        yy_out.println("Reduce: " + yy_productions[yy_prod_idx]);
                        break PARSING;
                    }
                    //E->E+E
                    case 2: {
                        double v1 = (double) yy_prod_body.get(0).getVal();
                        double v2 = (double) yy_prod_body.get(2).getVal();
                        yy_cur_tk.setVal(v1 + v2);
                    }
                    break;
                    //E->E-E
                    case 3: {
                        double v1 = (double) yy_prod_body.get(0).getVal();
                        double v2 = (double) yy_prod_body.get(2).getVal();
                        yy_cur_tk.setVal(v1 - v2);
                    }
                    break;
                    //E->E*E
                    case 4: {
                        double v1 = (double) yy_prod_body.get(0).getVal();
                        double v2 = (double) yy_prod_body.get(2).getVal();
                        yy_cur_tk.setVal(v1 * v2);
                    }
                    break;
                    //E->E/E
                    case 5: {
                        double v1 = (double) yy_prod_body.get(0).getVal();
                        double v2 = (double) yy_prod_body.get(2).getVal();
                        yy_cur_tk.setVal(v1 / v2);
                    }
                    break;
                    //E->E^E
                    case 6: {
                        double v1 = (double) yy_prod_body.get(0).getVal();
                        double v2 = (double) yy_prod_body.get(2).getVal();
                        yy_cur_tk.setVal(Math.pow(v1, v2));
                    }
                    break;
                    //E->-E
                    case 7: {
                        double v = (double) yy_prod_body.get(1).getVal();
                        yy_cur_tk.setVal(-v);
                    }
                    break;
                    //E->(E)
                    case 8: {
                        double v = (double) yy_prod_body.get(1).getVal();
                        yy_cur_tk.setVal(v);
                    }
                    break;
                    //E->NUM
                    case 9: {
                        double v = (double) yy_prod_body.get(0).getVal();
                        yy_cur_tk.setVal(v);
                    }
                    break;
                    default:
                        break;

                }

                //backtrack the state record, shift with the head of production
                yy_token_stack.push(yy_cur_tk);
                for (int i = 0; i < body_cnt; i++)
                    yy_state_stack.pop();

                //transfer from the whole production
                int yy_prod_head = yy_cur_tk.getName();
                yy_transfer = yy_next[yy_base[yy_cur_st()] + yy_prod_head];
                yy_state_stack.push(yy_transfer);

                //load yy_transfer fro shift, yy_lookahead is the same
                yy_transfer = yy_next[yy_base[yy_cur_st()] + yy_lookahead];

                yy_out.println("Reduce: " + yy_productions[yy_prod_idx]);


            } else {
                yy_error();
                return FAILURE;
            }
        }
        yy_out.printf("The calculation result is: %f\n", (double) yy_token_stack.peek().getVal());
        return SUCCESS;
    }

    private void yy_next_look() {
        yy_lookahead = yy_token_map[yylex.yylex()];
        yy_transfer = yy_next[yy_base[yy_cur_st()] + yy_lookahead];
    }

    private void yy_error() {
        System.out.println("ERROR");
    }

    private int yy_cur_st() {
        return yy_state_stack.isEmpty() ? -1 : yy_state_stack.peek();
    }


    private Object yylval() {
        return yylex.getYylval();
    }

    private void yy_set_printer() throws IOException {
        yy_out = new PrintStream(TARGET);
    }

}
