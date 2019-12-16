package lex.fa;

import lex.Global;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility that help translate DFA into useful tables
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class FAUtil {
    private final static int ASCII_LEN = 128;

    public static void translate(DFA dfa, List<int[]> tables, List<Integer> actionIdxes) {
        tables.clear();
        actionIdxes.clear();
        List<DFANode> dfaNodes = new ArrayList<>(dfa.getNodes());
        int fullSetSize = Global.FULL_CHARSET.size();

        int[] charset = new int[ASCII_LEN];
        //-1 is illegal value, indicating not-supported char
        Arrays.fill(charset, -1);
        int charCnt = 0;
        //starts with 0
        for (char c : Global.FULL_CHARSET) {
            charset[(int) c] = charCnt++;
        }
        tables.add(charset);

        int[] base = new int[dfaNodes.size()];
        int[] next = new int[base.length * fullSetSize];
        Arrays.fill(next, -1);

        DFANode curNode;
        for (int i = 0; i < dfaNodes.size(); i++) {
            curNode = dfaNodes.get(i);
            int state = curNode.getState();
            //each state has the condition space: size of ascii charset
            base[state] = i * fullSetSize;
            for (char condition : curNode.getConditions()) {
                //map possible conditions to destination
                next[base[state] + charset[(int) condition]] = curNode.transfer(condition);
            }
        }
        tables.add(base);
        tables.add(next);

        Map<Integer, Integer> actIdxes = dfa.getActIdxMap();
        //the order of dfa accept states, will be used as the index of corresponding accept state in accepts table
        // and its action in actIdxes
        int[] accepts = new int[dfaNodes.size()];
        Arrays.fill(accepts, -1);
        for (int i : dfa.getAccepts()) {
            accepts[i] = actIdxes.get(i);
//            accepts[i] = acceptOrder++;
//            actionIdxes.add(actIdxes.get(i));
        }
        tables.add(accepts);
    }

    public static DFA transform2DFA(NFA nfa) throws FAException {
        return new DFA(nfa);
    }

    public static NFA mergeNFA(List<NFA> nfaList) throws FAException {
        assert (nfaList.size() > 0);
        for (NFA nfa : nfaList)
            nfa.mature();
        NFA res = nfaList.get(0);
        for (int i = 1; i < nfaList.size(); i++)
            res.or(nfaList.get(i));
        return res;
    }

    static boolean setEquals(Set<Integer> s1, Set<Integer> s2) {
        if (s1.size() != s2.size())
            return false;
        else {
            List<Integer> l1 = s1.stream().sorted().collect(Collectors.toList());
            List<Integer> l2 = s2.stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < l1.size(); i++)
                if (!l1.get(i).equals(l2.get(i)))
                    return false;
            return true;
        }
    }
}
