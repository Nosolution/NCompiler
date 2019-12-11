import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RENormalizer {

    public static String normalize(TreeMap<String, String> terms) {
        for (String key : terms.keySet()) {
            String term = terms.get(key);
            terms.replace(key, replaceEscape(term));
        }

        freeContext(terms);


        return null;
    }

    private static String replaceEscape(String regex) {
        boolean inBracket = false;
        boolean inMark = false;
        StringBuilder res = new StringBuilder("");
        for (int i = 0; i < regex.length() - 1; i++) {
            if (regex.charAt(i) == '\\' && ((i > 0 && regex.charAt(i - 1) != '\\') || i == 0) && !inBracket && !inMark) {
                res.append('`');
                res.append(regex.charAt(++i));
            } else {
                if ((regex.charAt(i) == '[' || regex.charAt(i) == ']') && !inMark)
                    inBracket = !inBracket;
                else if (regex.charAt(i) == '\"' && !inBracket)
                    inMark = !inMark;
                res.append(regex.charAt(i));
            }
        }

        return res.toString();
    }

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

    private static String standardize(String regex) {


        return null;
    }

    /**
     * Transform the regex in bracket into standard form
     *
     * @param regex [X]
     * @return the result
     */
    private static String transformBracket(String regex) {
        boolean completement = regex.charAt(1) == '^';
        StringBuilder charSet = new StringBuilder("");
        if (regex.contains("-") && regex.charAt(2) != '-') {

        }
        return null;
    }

    private static String getCharSetOfInterval(char st, char ed) {
        StringBuilder res = new StringBuilder("");
        if (Character.isDigit(st) && Character.isDigit(ed)) {
            for (char c = st; c <= ed; c++) {
                res.append(c);
            }
        } else if (Character.isLetter(st) && Character.isLetter(ed)) {
            if (ed - st <= 'z' - 'a') {

            }
        }
    }

    /**
     * Contact each element in regex with dot
     *
     * @param regex target regex, such as abcd...
     * @return the result
     */
    private static String contact(String regex) {
        if (regex.length() <= 1)
            return regex;

        StringBuilder res = new StringBuilder("");
        res.append(regex.charAt(0));
        for (int i = 1; i < regex.length(); i++) {
            res.append('·').append(regex.charAt(i));
        }
        return res.toString();
    }


}
