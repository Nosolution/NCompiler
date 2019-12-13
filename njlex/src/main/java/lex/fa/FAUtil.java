package lex.fa;

import lex.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility that help translate DFA into useful arrays
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/13
 */
public class FAUtil {
    private final static int ASCII_LEN = 128;

    public static void translate(DFA dfa, List<int[]> arrays, List<String> actions) {
        arrays.clear();
        actions.clear();
        List<DFANode> dfaNodes = new ArrayList<>(dfa.getNodes());
        int fullSetSize = Global.FULL_CHARSET.size();

        int[] ec = new int[ASCII_LEN];
        int charCnt = 1;
        for (char c : Global.FULL_CHARSET) {
            ec[(int) c] = charCnt++;
        }
        arrays.add(ec);

        int[] base = new int[dfaNodes.size()];
        int[] next = new int[base.length * (fullSetSize + 1)];
        Arrays.fill(next, -1);

        DFANode curNode;
        for (int i = 0; i < dfaNodes.size(); i++) {
            curNode = dfaNodes.get(i);
            int state = curNode.getState();
            base[state] = i * (fullSetSize + 1);
            for (char condition : curNode.getConditions()) {
                next[base[state] + ec[(int) condition]] = curNode.transfer(condition);
            }
        }
        arrays.add(base);
        arrays.add(next);

        Map<Integer, String> actionMap = dfa.getActions();
        int acceptNumber = 1;
        int[] accepts = new int[dfaNodes.size()];
        for (int i : dfa.getAccepts()) {
            accepts[acceptNumber++] = i;
            actions.add(actionMap.get(i));
        }
        arrays.add(accepts);

    }
}
