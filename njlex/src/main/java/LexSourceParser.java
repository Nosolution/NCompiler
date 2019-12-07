import entity.Rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public boolean parse(String path, Map<String, String> terms, List<Rule> rules, List<String> defs, List<String> subroutines) {
        State st = State.start;
        String line;
        int lineCount = 0;
        boolean error = false;
        int brace_nest = 0;
        BufferedReader in = null;
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
                            if (term_pair.length != 2)
                                error = true;
                            else
                                terms.put(term_pair[0], term_pair[1]);
                        }
                        break;
                    case rule:
                        if (line.equals("%%"))
                            st = State.subroutine;
                        else {
                            if (line.startsWith(" ") || line.startsWith("\t")) {
                                if (line.contains("}")){
                                    brace_nest--;
                                    if(brace_nest == 0)
                                        continue;
                                }
                                else if(line.contains("{"))
                                    brace_nest++;

                                Rule last = rules.get(rules.size() - 1);
                                last.setAction(last.getAction() + line + "\n");
                            } else {
                                Rule rule = new Rule();
                                String[] rule_action = line.split(" ");
                                rule.setPattern(rule_action[0]);
                                if (!rule_action[1].equals("{"))
                                    rule.setAction(rule_action[1] + "\n");
                                else
                                    brace_nest++;
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

        if (lineCount == 0 || error)
            return false;



        return true;

    }
}
