package lex;

import lex.entity.Rule;
import lex.exceptions.ParseException;
import lex.fa.DFA;
import lex.fa.FAException;
import lex.fa.FAUtil;
import lex.fa.NFA;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Main entry of program
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class Main {

    public static void main(String[] args) {
        assert (args.length == 3);
        assert (args[1].equals("lex"));
        String path = args[2];
        TreeMap<String, String> terms = new TreeMap<>();
        List<Rule> rules = new ArrayList<>();
        List<String> defs = new ArrayList<>();
        List<String> subroutines = new ArrayList<>();

        System.out.println("Parsing .l file...");
        if (!LexSourceParser.parse(path, terms, rules, defs, subroutines)) {
            System.err.println("Failed to parse .l file");
            return;
        }
        System.out.println("COMPLETED");


        System.out.println("Normalizing REs...");
        try {
            ReNormalizer.normalize(rules, terms);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("COMPLETED");

        NFA nfa = null;
        System.out.println("Translating REs to NFAs...");
        try {
            for (Rule rule : rules) {
                NFA curNFA = ReTranslator.translateRe2NFA(rule.getPattern(), rule.getAction());
                if (nfa == null)
                    nfa = curNFA;
                else
                    nfa.or(curNFA);
            }
        } catch (ParseException | FAException e) {
            System.err.println("Failed to translate REs to NFAs");
            return;
        }
        System.out.println("COMPLETED");

        System.out.println("Transforming NFA to DFA...");
        DFA dfa = null;
        try {
            dfa = nfa == null ? null : new DFA(nfa);
        } catch (FAException e) {
            System.err.println("Failed to transform NFA to DFA");
            return;
        }
        System.out.println("COMPLETED");

        List<int[]> arrays = new ArrayList<>();
        List<String> actions = new ArrayList<>();
        System.out.println("Translating DFA to arrays...");
        if (dfa != null)
            FAUtil.translate(dfa, arrays, actions);
        System.out.println("COMPLETED");


    }
}
