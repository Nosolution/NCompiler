package lex;

import lex.entity.Rule;
import lex.exceptions.ParseException;
import lex.fa.DFA;
import lex.fa.FAException;
import lex.fa.FAUtil;
import lex.fa.NFA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Main entry of program
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class Main {

    /**
     * @param args <executable name> lex <src-path>
     */
    public static void main(String[] args) {
        assert (args.length == 2);
        assert (args[0].equals("lex"));
        String path = args[1];
        TreeMap<String, String> terms = new TreeMap<>();
        List<Rule> rules = new ArrayList<>();
        List<String> rawDefs = new ArrayList<>();
        List<String> localDefs = new ArrayList<>();
        List<String> userCode = new ArrayList<>();

        System.out.println("Parsing .l file...");
        if (!LexSourceParser.parse(path, terms, rules, rawDefs, localDefs, userCode)) {
            System.err.println("Failed to parse .l file");
            return;
        }
        System.out.println("COMPLETED");


        System.out.println("Normalizing REs...");
        try {
            ReNormalizer.normalize(rules, terms);
        } catch (ParseException e) {
            System.err.println("Failed to normalize REs");
            e.printStackTrace();
            return;
        }
        System.out.println("COMPLETED");

        NFA nfa;
        List<NFA> nfaList = new ArrayList<>();
        System.out.println("Translating REs to NFAs...");
        try {
            for (int i = 0; i < rules.size(); i++) {
                NFA curNFA = ReTranslator.translateRe2NFA(rules.get(i).getPattern(), i);
                nfaList.add(curNFA);
            }
            nfa = nfaList.size() > 0 ? FAUtil.mergeNFA(nfaList) : null;
        } catch (ParseException | FAException e) {
            System.err.println("Failed to translate REs to NFA");
            e.printStackTrace();
            return;
        }
        System.out.println("COMPLETED");

        System.out.println("Transforming NFA to DFA...");
        DFA dfa;
        try {
            dfa = nfa == null ? null : FAUtil.transform2DFA(nfa);
        } catch (FAException e) {
            System.err.println("Failed to transform NFA to DFA");
            e.printStackTrace();
            return;
        }
        System.out.println("COMPLETED");

        System.out.println("Minimizing DFA...");
        if (dfa != null)
//                dfa.minimize();
            dfa = dfa.minimize();
        System.out.println("COMPLETED");

        List<int[]> tables = new ArrayList<>();
        List<Integer> actionIdxes = new ArrayList<>();
        System.out.println("Translating DFA to arrays...");
        if (dfa != null)
            FAUtil.translate(dfa, tables, actionIdxes);
        System.out.println("COMPLETED");

        System.out.println("Generating codes...");
        CodeGenerator generator = new CodeGenerator();
        try {
//            generator.generate(tables,
//                    actionIdxes.stream().map(o -> rules.get(o).getAction()).collect(Collectors.toList()),
//                    dfa == null ? 0 : dfa.getInitialState(),
//                    rawDefs,
//                    localDefs,
//                    userCode);
            generator.generate(tables,
                    rules.stream().map(Rule::getAction).collect(Collectors.toList()),
//                    actionIdxes.stream().map(o -> rules.get(o).getAction()).collect(Collectors.toList()),
                    dfa == null ? 0 : dfa.getInitialState(),
                    rawDefs,
                    localDefs,
                    userCode);
        } catch (IOException e) {
            System.err.println("Failed to generate code");
            e.printStackTrace();
            return;
        }
        System.out.println("COMPLETED");


    }
}
