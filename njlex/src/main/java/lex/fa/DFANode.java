package lex.fa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Node class of deterministic finite automaton, containing source state and transition conditions and destinations
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */
public class DFANode {
    private int state;
    private Map<Character, Integer> transitions;
    private Set<Character> conditions;

    public DFANode(int state) {
        this.state = state;
        transitions = new HashMap<>();
        conditions = null;
    }

    public void addTransition(char condition, int dest) {
        transitions.put(condition, dest);
        if (conditions != null)
            conditions.add(condition);
    }

    public int getState() {
        return state;
    }

    public Set<Character> getConditions() {
        if (conditions == null)
            conditions = transitions.keySet();
        return conditions;
    }

    public int transfer(char condition) {
        return transitions.getOrDefault(condition, -1);
    }


}
