package lex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Global {
    //Currently supported charset
    public final static Set<Character> FULL_CHARSET = new HashSet<>(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '!', '\"', '#', '%', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '[', '\\', ']', '^', '{', '|', '}', '_', ' ', '\n', '\t', '\f', '\0', '~', '`', '@', '&'));
    //Operators in standard REs, implying that it should be escaped to use its literal meaning.
    public final static Set<Character> STANDARD_OPERATOR = new HashSet<>(Arrays.asList('.', '|', '*', '\\', '(', ')'));
    /*
    Extended operators are `.'(with dramatically different meaning compared to standard ver), `+', `?', `[', `]', `{', `}',`"', `^' and `-' in brackets([])
    `|', `*', `(', `)', the four one are also included in extended RE operators, keeping their meanings unchanged.
    Note that the standard version of operators is a SUBSET of the extended one.
     */

    public final static char ESCAPE = '\\';
    //Using control char to prevent conflicts
    public final static char EPSILON = '\b';
}
