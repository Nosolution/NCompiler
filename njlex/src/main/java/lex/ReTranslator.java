package lex;

import lex.exceptions.ParseException;
import lex.fa.FAException;
import lex.fa.NFA;

import java.util.*;

/**
 * Class with static method that translate a standardized RE to a NFA
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/11
 */
public class ReTranslator {
    private final static Set<Character> UNARY = new HashSet<>(Collections.singletonList('*'));
    private final static Map<Character, Integer> BINARY = new HashMap<Character, Integer>() {
        {
            BINARY.put('.', 2);
            BINARY.put('|', 1);
        }
    };
    private final static char ESCAPE = '`';

    /**
     * Only entering entry of this RE parser class, translate Re into an NFA.
     * RE must have been standardized.
     *
     * @param regex Source RE
     * @return NFA constructed basing on the source RE
     * @throws ParseException Will be thrown when something wrong occurs in parsing RE
     */
    public static NFA translateRe2NFA(String regex, String action) throws ParseException {
        return toNFA(toSuffix(regex), action);
    }


    /**
     * Transform a RE in infix form to suffix form
     *
     * @param regex Target regex
     * @return Regex in suffix form
     */
    private static String toSuffix(String regex) {
        StringBuilder res = new StringBuilder();
        Stack<Character> ops = new Stack<>();
        char cur, next;
        for (int i = 0; i < regex.length() - 1; i++) {
            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if (cur == ESCAPE) {
                //non-operand char in standard regex need to keep escaped
                switch (next) {
                    case '(':
                    case ')':
                    case '.':
                    case '|':
                    case '*':
                        res.append(cur);
                        break;
                    default:
                        break;
                }
                res.append(next);
                i++;
            } else if (isOperand(cur))
                res.append(cur);
            else {
                if (cur == '(')
                    ops.push(cur);
                else if (cur == ')') {
                    char c = ops.pop();
                    while (c != '(') {
                        res.append(c);
                        c = ops.pop();
                    }
                } else if (UNARY.contains(cur)) {
                    res.append(cur);
                } else {
                    while (ops.size() > 0 && ops.peek() != '(' && BINARY.get(ops.peek()) >= BINARY.get(cur)) {
                        res.append(ops.pop());
                    }
                    ops.push(cur);
                }
            }
        }
        res.append(regex.charAt(regex.length() - 1));
        while (ops.size() > 0) {
            res.append(ops.pop());
        }
        return res.toString();
    }

    /**
     * Transform a RE in suffix form into NFA
     *
     * @param regex Source Re
     * @return The constructed NFA
     * @throws ParseException Will be thrown when something wrong occurs in transforming
     */
    private static NFA toNFA(String regex, String action) throws ParseException {
        Stack<NFA> nfaStk = new Stack<>();

        char cur, next;
        for (int i = 0; i < regex.length() - 1; i++) {
            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if (cur == ESCAPE) {
                nfaStk.push(new NFA(next));
            } else if (isOperand(cur)) {
                nfaStk.push(new NFA(cur));
            } else {
                NFA top = nfaStk.pop();
                NFA sub;
                try {
                    switch (cur) {
                        case '.': {
                            sub = nfaStk.pop();
                            nfaStk.push(sub.concat(top));
                            break;
                        }
                        case '|': {
                            sub = nfaStk.pop();
                            nfaStk.push(sub.or(top));
                            break;
                        }
                        case '*':
                            nfaStk.push(top.repeat());
                            break;
                        default:
                            break;
                    }
                } catch (FAException e) {
                    throw new ParseException("Failed to translate RE");
                }
            }
        }
        if (nfaStk.size() != 1)
            throw new ParseException("Failed to translate RE");

        NFA res = nfaStk.peek();
        try {
            //need to ensure nfa constructed above has only one accept state
            res.addActions(res.getAccepts().stream().findFirst().get(), action);
        } catch (FAException e) {
            throw new ParseException("Failed to translate RE");
        }
        return res;
    }

    /**
     * Judge if a char is an operand
     *
     * @param c The char to be judged
     * @return True if is operand, false otherwise
     */
    private static boolean isOperand(char c) {
        return !UNARY.contains(c) && !BINARY.containsKey(c) && c != '(' && c != ')';
    }

}
