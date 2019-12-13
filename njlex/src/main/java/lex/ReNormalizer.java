package lex;

import lex.entity.Rule;
import lex.exceptions.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ReNormalizer {

    public static void normalize(List<Rule> rules, TreeMap<String, String> terms) throws ParseException {
//        for (String key : terms.keySet()) {
//            String term = terms.get(key);
//            terms.replace(key, replaceEscape(term));
//        }

        freeContext(terms);
        for (Rule rule : rules) {
            rule.setPattern(standardize(replaceTerms(rule.getPattern(), terms)));
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
                regex = regex.replaceAll("{" + key + "}", terms.get(key));
            }
        }
        return regex;
    }

    /**
     * Replace normal escape char '\\' with program-specified escape char '`'
     *
     * @param regex Target RE
     * @return Result of replacement
     */
    private static String replaceEscape(String regex) {
        boolean inBracket = false;
        boolean inQuote = false;
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < regex.length() - 1; i++) {
            if (regex.charAt(i) == '\\' && ((i > 0 && regex.charAt(i - 1) != '\\') || i == 0) && !inBracket && !inQuote) {
                res.append('`');
                res.append(regex.charAt(++i));
            } else {
                if (!inQuote && (regex.charAt(i) == '[' || regex.charAt(i) == ']'))
                    inBracket = !inBracket;
                else if (regex.charAt(i) == '\"' && !inBracket)
                    inQuote = !inQuote;
                res.append(regex.charAt(i));
            }
        }
        return res.toString();
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
                    String res = term.replaceAll("{" + s + "}", terms.get(s));
                    terms.replace(key, res);
                }
            }
            keys.add(key);
        }
    }

    /**
     * Transform a RE to standard form
     * Standard form implies there are only 3 operators '.', '|', '*'， 1 escape char '`' and 1 pair of parenthesis
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
        for (int i = 0; i < regex.length() - 1; i++) {
            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if ((leftBracketIdx == -1) && (leftQuoteIdx == -1)) {
                if (cur != '\\') {
                    switch (cur) {
                        case '[':
                            leftBracketIdx = i;
                            break;
                        case '\"':
                            leftQuoteIdx = i;
                            break;
                        case '+':
                        case '?': {
                            if (i == 0) {
                                throw new ParseException(cur + " operator shall not be put at the first place");
                            }
                            String formerStr = getFormerBasicStr(regex, i);
                            if (cur == '+')
                                res.append(formerStr).append(formerStr).append('*');
                            else
                                res.append("(@|").append(formerStr).append(")");
                        }
                        break;
                        case '{': {
                            if (i == 0) {
                                throw new ParseException(" Repeat operator shall not be put at the first place");
                            }
                            int comma = regex.indexOf(',', i + 2);
                            if (comma == -1)
                                throw new ParseException("Wrong format");
                            int minimum = Integer.parseInt(regex.substring(i + 1, comma));
                            int rightBrace = regex.indexOf('}', comma + 2);
                            if (rightBrace == -1)
                                throw new ParseException("Wrong format");
                            int maximum = Integer.parseInt(regex.substring(comma + 1, rightBrace));
                            String formerStr = getFormerBasicStr(regex, i);
                            res.append(repeat(formerStr, minimum, maximum));

                        }
                        case '.':
                            res.append(transformDot());
                            break;
                        default:
                            res.append(cur);
                            break;
                    }
                } else {
                    res.append('`');
                    switch (next) {
                        case 'n':
                            res.append('\n');
                            break;
                        case 't':
                            res.append('\t');
                            break;
                        case 'f':
                            res.append('\f');
                            break;
                        default:
                            res.append(next);
                            break;
                    }
                    i++;
                }
            } else if (leftBracketIdx != -1) {
                if (cur == ']') {
                    res.append(transformBracket(regex.substring(leftBracketIdx, i + 1)));
                    leftBracketIdx = -1;
                }
            } else {//transform from quote
                if (cur == '\\' && next == '\"') {
                    res.append(next);
                    i++;
                } else if (cur == '\\' && next == '\\') {
                    res.append('`').append(next);
                    i++;
                } else if (cur == '\"')
                    leftQuoteIdx = -1;
                else {
                    if (Global.ESCAPE_CHARSET.contains(cur))
                        res.append('`');
                    res.append(cur);
                }
            }
        }

        return joinDot(res.toString());
    }

    /**
     * Transform the RE in bracket into standard form
     *
     * @param regex [X]
     * @return the result
     */
    private static String transformBracket(String regex) throws ParseException {
        boolean complement = regex.charAt(1) == '^';
        StringBuilder charSet = new StringBuilder();
        char cur, next;
        for (int i = 1; i < regex.length() - 1; i++) {
            charSet.append("|");

            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if (next == '-') {
                charSet.append(getCharSetOfInterval(cur, regex.charAt(i + 2)));
                i += 2;
            } else {
                if (cur == '\\') {
                    charSet.append('`');
                    switch (next) {
                        case 'n':
                            charSet.append('\n');
                            break;
                        case 't':
                            charSet.append('\t');
                            break;
                        case 'f':
                            charSet.append('\f');
                            break;
                        case '\'':
                        case '\"':
                        case '\\':
                            charSet.append(next);
                            break;
                        default:
                            break;

                    }
                    i++;
                } else
                    charSet.append(cur);
            }
        }
//        charSet.append(regex.charAt(regex.length() - 2));
        return "(" + String.join("|", toStrList(complement ? getComplement(charSet.toString()) : charSet.toString())) + ")";
    }

    /**
     * Transform content in a pair of quote into standard RE
     *
     * @param regex Target regex
     * @return The result of transformation
     */
    private static String transformQuote(String regex) {
        StringBuilder res = new StringBuilder();
        char cur, next;
        for (int i = 1; i < regex.length() - 1; i++) {
            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if (cur == '\\') { // next == '\"' or next == '\\'
                res.append('`').append(next);
                i++;
            } else {
                if (Global.ESCAPE_CHARSET.contains(cur))
                    res.append('`');
                res.append(cur);
            }
        }
        return res.toString();
    }

    /**
     * Generate standard RE representing dot
     *
     * @return The result
     */
    private static String transformDot() {
        return getComplement("\n");
    }

    /**
     * Construct standard RE representing repetition
     *
     * @param source  Source string which will repeat specified times
     * @param minimum Minimum of repetitions
     * @param maximum Maximum of repetitions
     * @return The repetition result
     */
    private static String repeat(String source, int minimum, int maximum) {
        return doRepeat("(" + source + ")", minimum, maximum);
    }

//    private static String repeat(char c, int minimum, int maximum) {
//        return doRepeat("(" + c + ")", minimum, maximum);
//    }

    /**
     * Truly do repetition, in order to be compatible to char and str
     *
     * @param source  Source string which will repeat specified times
     * @param minimum Minimum of repetitions
     * @param maximum Maximum of repetitions
     * @return The result after
     */
    private static String doRepeat(String source, int minimum, int maximum) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < minimum; i++) {
            res.append(source);
        }
        for (int i = minimum; i < maximum; i++) {
            res.append("(@|").append(source).append(")");
        }
        return res.toString();
    }

    /**
     * Join each element of RE with a dot
     *
     * @param regex Target RE
     * @return Result of joining dots
     */
    private static String joinDot(String regex) {
        StringBuilder res = new StringBuilder();
        char cur, next;
        for (int i = 0; i < regex.length() - 1; i++) {
            cur = regex.charAt(i);
            next = regex.charAt(i + 1);
            if (cur == '`') {
                res.append(cur).append(next);
                i++;
            } else if (cur == '(' || cur == '|' || cur == '*') {
                continue;
            } else {
                res.append(cur);
            }
            res.append('.');
        }
        res.append(regex.charAt(regex.length() - 1));
        return res.toString();

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
     * get the complement set of given charset in terms of FULLCHARSET
     *
     * @param source Source of char set
     * @return The complement of given char set
     */
    private static String getComplement(String source) {
        StringBuilder res = new StringBuilder();
        for (char c : Global.FULL_CHARSET) {
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
        if (str.charAt(fromIndex - 1) == ')' && str.charAt(fromIndex - 2) != '\\') {
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


    /**
     * Convert single str to list of elements representing by str
     *
     * @param str Target str
     * @return List of strings containing the element of source str
     */
    private static List<String> toStrList(String str) {
        return str.chars()
                .mapToObj(c -> (char) c)
                .map(c -> Global.ESCAPE_CHARSET.contains(c) ? ("\\" + c) : String.valueOf(c))
                .collect(Collectors.toList());
    }


}
