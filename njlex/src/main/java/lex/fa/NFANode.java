package lex.fa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Node class of non-deterministic finite automaton, containing source state and transition conditions and destinations
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */
public class NFANode {
    private int state;
    private List<Tuple<Character, Integer>> transitions;
    private Set<Character> conditions;

    NFANode(int state) {
        this.state = state;
        transitions = new ArrayList<>();
        conditions = null;
    }

    public void addTransition(char condition, int dest) {
        if (transitions.stream().noneMatch(o -> o.getFirst() == condition && o.getSecond() == dest)) {
            transitions.add(new Tuple<>(condition, dest));
            if (conditions != null)
                conditions.add(condition);
        }
    }

    public int getState() {
        return state;
    }

    public Set<Character> getConditions() {
        if (conditions == null)
            conditions = transitions.stream().map(Tuple::getFirst).collect(Collectors.toSet());
        return conditions;
    }

    public Set<Integer> transfer(char condition) {
        return transitions.stream()
                .filter(o -> o.getFirst() == condition)
                .map(Tuple::getSecond)
                .collect(Collectors.toSet());
    }

}
