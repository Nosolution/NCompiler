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
    private int state; //identify state number
    private Map<Character, Integer> transitions; //transition map, using map instead of list can increase efficiency
    private Set<Character> conditions; //all possible conditions

    public DFANode(int state) {
        this.state = state;
        transitions = new HashMap<>();
        conditions = null;
    }

    /**
     * Add new transition, map the condition to the destination.
     *
     * @param condition given condition
     * @param dest      transferring destination
     */
    public void addTransition(char condition, int dest) {
        transitions.put(condition, dest);
        if (conditions != null)
            conditions.add(condition);
    }

    /**
     * Update the destination of already-existed condition
     *
     * @param condition the given condition
     * @param dest      new destination
     * @throws FAException if the given condition hasn't been set previously
     */
    public void updateTransition(char condition, int dest) throws FAException {
        if (transitions.containsKey(condition))
            //reuse addTransition method because of the specific implementation of java map
            addTransition(condition, dest);
        else
            throw new FAException("No such a condition");
    }

    /**
     * Get the identify state number of this node
     */
    public int getState() {
        return state;
    }

    /**
     * Get the condition set
     */
    public Set<Character> getConditions() {
        if (conditions == null)
            conditions = transitions.keySet();
        return conditions;
    }

    /**
     * Transfer by given condition
     *
     * @param condition given condition
     * @return the result of transferring, if the given condition is not in the previously set condition set, {@code -1} will be returned
     */
    public int transfer(char condition) {
        return transitions.getOrDefault(condition, -1);
    }

    void changeState(int newState) {
        state = newState;
    }


}
