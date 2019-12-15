package lex.fa;

import lex.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility that help translate DFA into useful tables
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class FAUtil {
    private final static int ASCII_LEN = 128;

    public static void translate(DFA dfa, List<int[]> tables, List<Integer> actIdxes) {
        tables.clear();
        actIdxes.clear();
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

        Map<Integer, Integer> actionMap = dfa.getActIdxes();
        //the order of dfa accept states, will be used as the index of corresponding accept state in accepts table
        // and its action in actIdxes
        int acceptOrder = 0;
        int[] accepts = new int[dfaNodes.size()];
        for (int i : dfa.getAccepts()) {
            accepts[acceptOrder++] = i;
            actIdxes.add(actionMap.get(i));
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
}
