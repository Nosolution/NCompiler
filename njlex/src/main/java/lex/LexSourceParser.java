package lex;

import lex.entity.Rule;
import lex.exceptions.ParseException;

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


    private final static String DEFAULT_ACTION = "ECHO";

    private LexSourceParser() {
    }

    /**
     * Parse given .l file
     *
     * @param path     path of .l file
     * @param terms    location to store user-defined regex terms
     * @param rules    location to store regex rules including actions
     * @param rawDefs  definitions that will be copied into generated codes
     * @param userCode user provide subroutines that will be used in actions
     * @return True if parse successfully, otherwise false
     */
    public static boolean parse(String path, TreeMap<String, String> terms, List<Rule> rules, List<String> rawDefs, List<String> localDefs, List<String> userCode) {
        State st = State.raw;
        String line;
        int lineCount = 0;
        boolean inBrace = false;
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
            while (true) {
                line = in.readLine();
                if (line == null)
                    break;
                lineCount++;


                switch (st) {
                    case raw:
                        if (inBrace) {
                            if (line.equals("%}"))
                                inBrace = false;
                            else
                                rawDefs.add(line.trim() + "\n");
                            continue;
                        } else {
                            if (line.equals("%{")) {
                                inBrace = true;
                                continue;
                            } else if (line.startsWith(" ") || line.startsWith("\t")) {
                                rawDefs.add(line.trim() + "\n");
                                continue;
                            } else {
                                //Didn't satisfy ant format of raw part, change state,
                                //go on with the code in case term
                                st = State.term;
                            }
                        }
                    case term:
                        if (line.equals("%%"))
                            st = State.local;
                        else {
                            String[] term_pair = line.split("[ \t]+");
                            if (term_pair.length != 2)
                                throw new ParseException("Failed to parse definition part");
                            else {
                                for (int i = 0; i < term_pair[0].length(); i++) {
                                    if (!Character.isAlphabetic(term_pair[0].charAt(i))) {
                                        throw new ParseException("Failed to parse definition part");
                                    }
                                }
                                terms.put(term_pair[0], term_pair[1]);
                            }
                        }
                        break;
                    case local:
                        if (inBrace) {
                            if (line.equals("%}"))
                                inBrace = false;
                            else
                                localDefs.add(line.trim() + "\n");
                            continue;
                        } else {
                            if (line.equals("%{")) {
                                inBrace = true;
                                continue;
                            } else if (line.startsWith(" ") || line.startsWith("\t")) {
                                localDefs.add(line.trim() + "\n");
                                continue;
                            } else {
                                //Didn't satisfy ant format of local part, change state,
                                //go on with the code in case rule
                                st = State.rule;
                            }
                        }
                    case rule:
                        if (line.equals("%%"))
                            st = State.userCode;
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
                                StringBuilder pattern = new StringBuilder();
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
                                if (action.length() == 0)
                                    rule.setAction(DEFAULT_ACTION + "\n");
                                else if (!action.equals("{"))
                                    rule.setAction(action + "\n");
                                else
                                    multi_actions = true;

                                rules.add(rule);
                            }
                        }
                        break;
                    case userCode:
                        userCode.add(line + "\n");
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return false;
        }

        //.l file　must have a least one line which is "%%"
        return lineCount != 0;

    }

    private enum State {raw, term, local, rule, userCode}

}
