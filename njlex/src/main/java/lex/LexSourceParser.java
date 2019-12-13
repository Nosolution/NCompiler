package lex;

import lex.entity.Rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * Parse .l file
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/6
 */
public class LexSourceParser {


    private static enum State {start, raw_def, term, rule, subroutine}

    private LexSourceParser() {
    }

    /**
     * Parse given .l file
     *
     * @param path        path of .l file
     * @param terms       location to store user-defined regex terms
     * @param rules       location to store regex rules including actions
     * @param defs        definitions that will be copied into generated codes
     * @param subroutines user provide subroutines that will be used in actions
     * @return True if parse successfully, otherwise false
     */
    public static boolean parse(String path, TreeMap<String, String> terms, List<Rule> rules, List<String> defs, List<String> subroutines) {
        State st = State.start;
        String line;
        int lineCount = 0;
        boolean error = false;
        boolean inBracket = false;
        boolean multi_actions = false;
        boolean inMark = false;
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(path));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            while (!error) {
                line = in.readLine();
                if (line == null)
                    break;
                lineCount++;

                switch (st) {
                    case start:
                        if (line.equals("%{"))
                            st = State.raw_def;
                        else if (line.equals("%%"))
                            st = State.rule;
                        else
                            st = State.term;
                        break;
                    case raw_def:
                        if (line.equals("%}"))
                            st = State.term;
                        else
                            defs.add(line);
                        break;
                    case term:
                        if (line.equals("%%"))
                            st = State.rule;
                        else {
                            String[] term_pair = line.split(" ");
                            if (term_pair.length == 1) {
                                term_pair = term_pair[0].split("\t");

                            }
                            if (term_pair.length != 2)
                                error = true;
                            else {
                                for (int i = 0; i < term_pair[0].length(); i++) {
                                    if (!Character.isAlphabetic(term_pair[0].charAt(i))) {
                                        error = true;
                                        break;
                                    }
                                }
                                terms.put(term_pair[0], term_pair[1]);
                            }
                        }
                        break;
                    case rule:
                        if (line.equals("%%"))
                            st = State.subroutine;
                        else {
                            line = line.trim();
                            if (multi_actions) {
                                if (line.equals("}")) {
                                    multi_actions = false;
                                } else {
                                    Rule last = rules.get(rules.size() - 1);
                                    last.setAction(last.getAction() + line + "\n");
                                }
                            } else {
                                Rule rule = new Rule();
                                StringBuilder pattern = new StringBuilder("");
                                int i = 0;
                                for (; i < line.length(); i++) {
                                    if (!inBracket) {
                                        if (line.charAt(i) == '[' && (i == 0 || line.charAt(i) != '\\'))
                                            inBracket = true;
                                    } else {
                                        if (line.charAt(i) == ']')
                                            inBracket = false;
                                    }
                                    if (line.charAt(i) == '\"' && (i == 0 || line.charAt(i) != '\\'))
                                        inMark = !inMark;
                                    if (!inBracket && !inMark && (line.charAt(i) == ' ' || line.charAt(i) == '\t'))
                                        break;
                                    pattern.append(line.charAt(i));
                                }
                                rule.setPattern(pattern.toString());

                                String action = line.substring(i).trim();
                                if (!action.equals("{")) {
                                    rule.setAction(action + "\n");
                                } else
                                    multi_actions = true;
                            }
                        }
                    case subroutine:
                        subroutines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //.l file　must have a least one line which is "%%"
        return lineCount != 0 && !error;

    }
}
