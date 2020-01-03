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
    private int state;  //identify state number
    private List<Tuple<Character, Integer>> transitions; //transitions represented by tuple(condition, destination)
    private Set<Character> conditions; //redundant condition set for efficiency

    NFANode(int state) {
        this.state = state;
        transitions = new ArrayList<>();
        conditions = null;
    }

    /**
     * Add a new transition for this node
     *
     * @param condition Transferring condition
     * @param dest      Destination for transferring
     */
    public void addTransition(char condition, int dest) {
        if (transitions.stream().noneMatch(o -> o.getFirst() == condition && o.getSecond() == dest)) {
            transitions.add(new Tuple<>(condition, dest));
            if (conditions != null)
                conditions.add(condition);
        }
    }

    /**
     * Get identify state number representing the node
     *
     * @return Identify state number
     */
    public int getState() {
        return state;
    }

    /**
     * Get all conditions that the node can transfer in
     *
     * @return Condition set
     */
    public Set<Character> getConditions() {
        if (conditions == null)
            conditions = transitions.stream().map(Tuple::getFirst).collect(Collectors.toSet());
        return conditions;
    }

    /**
     * Transfer according to given condition
     *
     * @param condition Given condition
     * @return The transferring destination set for the node is nfa node. Empty set may be return if under no condition can this node transfer
     */
    public Set<Integer> transfer(char condition) {
        return transitions.stream()
                .filter(o -> o.getFirst() == condition)
                .map(Tuple::getSecond)
                .collect(Collectors.toSet());
    }

}
