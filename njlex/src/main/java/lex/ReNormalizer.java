package lex;

import lex.entity.Rule;
import lex.exceptions.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static lex.Global.*;

public class ReNormalizer {

    public static void normalize(List<Rule> rules, TreeMap<String, String> terms) throws ParseException {
        freeContext(terms);
        rules.stream().filter(o -> o.getPattern().length() == 0).forEach(o -> System.out.println(o.getPattern()));
        for (Rule rule : rules) {
            rule.setPattern(
                    standardize(
                            replaceTerms(rule.getPattern(), terms)));
        }
    }

    /**
     * In each RE, replace the user-customized terms with its content, to eliminate dependencies across expressions
     *
     * @param terms User-customized terms
     */
    private static void freeContext(TreeMap<String, String> terms) {
        List<String> keys = new ArrayList<>();
        for (String key : terms.keySet()) {
            String term = terms.get(key);
            for (String s : keys) {
                if (term.contains("{" + s + "}")) {
                    String res = replaceAll(term, "{" + s + "}", terms.get(s));
                    terms.replace(key, res);
                }
            }
            keys.add(key);
        }
    }

    /**
     * Replace every user-customized term in source RE into its own content
     *
     * @param regex Source RE
     * @param terms Term dictionary containing names and their own contents
     * @return Result of replacement
     */
    private static String replaceTerms(String regex, Map<String, String> terms) {

        for (String key : terms.keySet()) {
            if (regex.contains("{" + key + "}")) {
                regex = replaceAll(regex, "{" + key + "}", terms.get(key));
            }
        }
        return regex;
    }

    private static String replaceAll(String src, String key, String replacement) {
        StringBuilder res = new StringBuilder();
        int prev = 0, cur = src.indexOf(key);
        while (cur != -1) {
            res.append(src, prev, cur).append(replacement);
            prev = cur + key.length();
            cur = src.indexOf(key, prev);
        }
        res.append(src, prev, src.length());
        return res.toString();
    }

    /**
     * Transform a RE to standard form
     * Standard form implies there are only 5 operators '.', '|', '*'，escape, epsilon, and 1 pair of parenthesis
     *
     * @param regex Source RE
     * @return Standardized RE
     * @throws ParseException Will be throw if source RE didn't satisfy the format of RE
     */
    private static String standardize(String regex) throws ParseException {
        StringBuilder res = new StringBuilder();
        int leftBracketIdx = -1;
        int leftQuoteIdx = -1;
        char cur, next;
        for (int i = 0; i < regex.length(); i++) {
            cur = regex.charAt(i);
            if (cur == ESCAPE && i == regex.length() - 1)
                throw new ParseException("Last char of regex cannot be escape char");
            next = (i == regex.length() - 1 ? 0 : regex.charAt(i + 1));
            //Not in the handling processes of [] and ""
            if ((leftBracketIdx == -1) && (leftQuoteIdx == -1)) {
                if (cur != ESCAPE) {
                    switch (cur) {
                        //Handle extended operators and translate them in standard ones
                        case '+':
                        case '?': {
                            if (i == 0) {
                                throw new ParseException(cur + " operator shall not be put at the first place");
                            }
                            String formerStr = getFormerBasicStr(res.toString(), res.length());
                            if (cur == '+')
                                res.append(formerStr).append('*');
                            else {
                                res.insert(res.length() - formerStr.length(), "(" + EPSILON + "|");
                                res.append(")");
                            }
                        }
                        break;
                        case '.':
                            res.append(translateDot());
                            break;
                        case '[':
                            leftBracketIdx = i;
                            break;
                        case '\"':
                            leftQuoteIdx = i;
                            break;
                        case '{': {
                            if (i == 0) {
                                throw new ParseException(" Repeat operator shall not be put at the first place");
                            }
                            int rightBrace = regex.indexOf('}', i + 1);
                            if (rightBrace == -1)
                                throw new ParseException("Wrong format");
                            int minimum, maximum;
                            int comma = regex.substring(i + 1, rightBrace).indexOf(',');
                            if (comma == -1) {//{x}
                                maximum = minimum = Integer.parseInt(regex.substring(i + 1, rightBrace));
                            } else {
                                minimum = Integer.parseInt(regex.substring(i + 1, comma));
                                maximum = comma == rightBrace - 1 ? -1 : Integer.parseInt(regex.substring(comma + 1, rightBrace));//{x,} or {x,y}
                            }
                            String formerStr = getFormerBasicStr(res.toString(), i);
                            res.append(repeat(formerStr, minimum, maximum));
                            break;
                        }
                        default:
                            //Standard operators and literal chars are copied to res
                            res.append(cur);
                            break;
                    }
                } else {
                    switch (next) {
                        //Have been escaped by the computer and meanings changed
                        case 'n':
                            res.append('\n');
                            break;
                        case 'r':
                            res.append('\r');
                        case 't':
                            res.append('\t');
                            break;
                        case 'f':
                            res.append('\f');
                            break;
                        case '0':
                            res.append('\0');
                            break;
                        default:
                            //If not, they are specially escaped extended operators, which need no escapes in standard REs.
                            if (STANDARD_OPERATOR.contains(next))
                                res.append(ESCAPE);
                            res.append(next);
                            break;
                    }
                    i++;
                }
            } else if (leftBracketIdx != -1) {
                //Handling process of []
                if (cur == ']') {
                    //Using subroutine will lose some efficiency but gain a more clear program.
                    res.append(translateBracket(regex.substring(leftBracketIdx, i + 1)));
                    leftBracketIdx = -1;
                }
            } else {
                //Handling process of ""
                //Considering of efficiency, chose not to handle quotes in a subroutine.
                //Because the contents in quotes are not sensitive to the context.
                if (cur == ESCAPE) {
                    if (next == '\"') {
                        res.append(next);
                        i++;
                    } else if (next == ESCAPE) {
                        res.append(ESCAPE).append(next);
                        i++;
                    } else
                        //In quotes, only quote mark and escape char themselves can be escaped.
                        throw new ParseException("Shall not escape char " + next + "in RE quotes");
                } else if (cur == '\"')
                    leftQuoteIdx = -1;
                else {
                    if (STANDARD_OPERATOR.contains(cur))
                        res.append(ESCAPE);
                    res.append(cur);
                }
            }
        }

        if (res.length() == 0)
            System.out.println(regex);
        return joinDots(res.toString());
    }

    /**
     * Translate the RE in bracket into standard form
     *
     * @param regex [X]
     * @return the result
     */
    private static String translateBracket(String regex) throws ParseException {
        boolean needComplete = regex.charAt(1) == '^';
        StringBuilder charSet = new StringBuilder();
        char cur, next;
        for (int i = needComplete ? 2 : 1; i < regex.length() - 1; i++) {
//            charSet.append("|");
            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if (next == '-') {
                if (Character.isLetterOrDigit(cur) && i < regex.length() - 3 && Character.isLetterOrDigit(regex.charAt(i + 2))) {
                    charSet.append(getCharSetOfInterval(cur, regex.charAt(i + 2)));
                    i += 2;
                } else {
                    charSet.append(cur).append(next);
                    i++;
                }
            } else {
                if (cur == ESCAPE) {
                    if (i == regex.length() - 2)
                        throw new ParseException("Last char in the bracket cannot be escape char");
                    switch (next) {
                        case 'n':
                            charSet.append('\n');
                            break;
                        case '\r':
                            charSet.append('\r');
                        case 't':
                            charSet.append('\t');
                            break;
                        case 'f':
                            charSet.append('\f');
                            break;
                        case '0':
                            charSet.append('\0');
                            break;
                        case '\\':
                            charSet.append('\\');
                            break;
                        default:
                            throw new ParseException("Shall not escape char " + next + " in brackets");
                    }
                    i++;
                } else
                    charSet.append(cur);
            }
        }
//        cur = regex.charAt(regex.length() - 2);
//        if (cur == ESCAPE)
//            throw new ParseException("Wrong escaping in brackets");
//        else
//            charSet.append(cur);
        return "(" + String.join("|", toStrList(needComplete ? getComplement(charSet.toString()) : charSet.toString())) + ")";
    }

    /**
     * Generate standard RE representing dot
     *
     * @return The result
     */
    private static String translateDot() {
        return "(" + String.join("|", toStrList(getComplement("\n"))) + ")";
    }

    /**
     * Construct standard RE representing repetition
     *
     * @param source  Source string which will repeat specified times
     * @param minimum Minimum of repetitions
     * @param maximum Maximum of repetitions
     * @return The result after
     */
    private static String repeat(String source, int minimum, int maximum) {
        source = "(" + source + ")";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < minimum; i++) {
            res.append(source);
        }
        if (maximum == -1) {
            res.append(source).append('*');
        } else {
            for (int i = minimum; i < maximum; i++) {
                res.append('(').append(EPSILON).append('|').append(source).append(")");
            }
        }
        return res.toString();
    }

    /**
     * Convert single str to list of elements representing by str
     *
     * @param str Target str
     * @return List of strings containing the element of source str
     */
    private static List<String> toStrList(String str) {
        return str.chars()
                .mapToObj(c -> (char) c)
                .map(c -> STANDARD_OPERATOR.contains(c) ? (ESCAPE + String.valueOf(c)) : String.valueOf(c))
                .collect(Collectors.toList());
    }

    /**
     * Join each element of RE with a dot
     *
     * @param regex Target RE
     * @return Result of joining dots
     */
    private static String joinDots(String regex) throws ParseException {
        StringBuilder res = new StringBuilder();
        char cur, next;
        for (int i = 0; i < regex.length(); i++) {
            cur = regex.charAt(i);

            if (cur == ESCAPE) {
                if (i == regex.length() - 1 || !STANDARD_OPERATOR.contains(regex.charAt(i + 1)))
                    throw new ParseException("Wrong escaping");
//                next = regex.charAt(i + 1);
                res.append(ESCAPE).append(regex.charAt(i + 1));
                i++;
            } else if (cur == '(') {
                int rightParenthesis = findRightParenthesis(regex, i + 1);
                if (rightParenthesis == -1)
                    throw new ParseException("Unclosed Parenthesis");
                else if (rightParenthesis > i + 1)
                    res.append("(").append(joinDots(regex.substring(i + 1, rightParenthesis))).append(")");
                i = rightParenthesis;
            } else
                res.append(cur);

            if (i < regex.length() - 1) {
                if (regex.charAt(i + 1) == '|') {
                    res.append(regex.charAt(++i));
                } else if (regex.charAt(i + 1) != '*') {
                    res.append('.');
                }
            }

        }

        return res.toString();

    }

    private static int findRightParenthesis(String src, int fromIndex) {
        int nest = -1, i = fromIndex;
        for (; i < src.length(); i++) {
            if (src.charAt(i) == ESCAPE)
                i++;
            else if (src.charAt(i) == '(')
                nest--;
            else if (src.charAt(i) == ')')
                nest++;
            if (nest == 0)
                break;
        }
        return nest == 0 ? i : -1;
    }

    /**
     * Get the char set of interval [st, ed]
     * Currently supported format: [Digit, Digit], [LowerCase, LowerCase], [UpperCase, UpperCase]
     * The starting char must lower in lexicographical order than ending char
     *
     * @param st Starting char
     * @param ed Ending char
     * @return The result
     * @throws ParseException Will be thrown when the format is not supported
     */
    private static String getCharSetOfInterval(char st, char ed) throws ParseException {
        StringBuilder res = new StringBuilder();
        if (Character.isDigit(st) && Character.isDigit(ed)) {
            if (ed < st)
                throw new ParseException("Ending char is prior to starting char");
            for (char c = st; c <= ed; c++) {
                res.append(c);
            }
        } else if ((Character.isLowerCase(st) && Character.isLowerCase(ed)) || (Character.isUpperCase(st) && Character.isUpperCase(ed))) {
            if (ed < st)
                throw new ParseException("Ending char is prior to starting char");
            for (char c = st; c <= ed; c++) {
                res.append(c);
            }
        } else {
            throw new ParseException("Not supported interval: " + st + "-" + ed);
        }
        return res.toString();
    }

    /**
     * Get the complement set of given charset in terms of FULL_CHARSET
     *
     * @param source Source of charset
     * @return The complement of given charset
     */
    private static String getComplement(String source) {
        StringBuilder res = new StringBuilder();
        for (char c : FULL_CHARSET) {
            if (source.indexOf(c) == -1)
                res.append(c);
        }
        return res.toString();
    }

    /**
     * Get former string from given index
     * Basic string meaning a single char or string between a pair of parenthesis
     *
     * @param str       Target string
     * @param fromIndex Starting position, which shall greater than 0
     * @return The result
     * @throws ParseException Will be Thrown when given index is equal or lower than 0
     */
    private static String getFormerBasicStr(String str, int fromIndex) throws ParseException {
        if (fromIndex <= 0) {
            throw new ParseException("Wrong index");
        }
        if (str.charAt(fromIndex - 1) == ')' && str.charAt(fromIndex - 2) != ESCAPE) {
            int leftParenthesis = str.lastIndexOf('(', fromIndex);
            while (leftParenthesis >= 1 && str.charAt(leftParenthesis - 1) == '\"')
                leftParenthesis = str.lastIndexOf('(', leftParenthesis - 1);
            if (leftParenthesis == -1)
                throw new ParseException("Parenthesis pairs didn't satisfy the format");
            return str.substring(leftParenthesis, fromIndex);
        } else {
            return str.substring(fromIndex - 1, fromIndex);
        }
    }


}
