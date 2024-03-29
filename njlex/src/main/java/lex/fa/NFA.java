package lex.fa;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lex.Global.EPSILON;

/**
 * Non-deterministic finite automaton that can translate specific expression
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */

public class NFA {
    private NFANode initial; //initial state, or in other words, start state
    private Set<Integer> accepts; //accept state set
    private Set<NFANode> nodes;   //node set owned be the nfa
    private Map<Integer, NFANode> nodeMap;//map that maps NFANode identify state number to its reference, redundant for high performance
    private Map<Integer, Integer> actIdxMap;//mapping node representing accept state to its action. Index is used to choose foremost matched action
    private boolean isMatured;

    public NFA() {
        new NFA(EPSILON);
    }

    public NFA(char transition) {
        this.initial = NFANodeFactory.produceNode();
        accepts = new HashSet<>();
        nodes = new HashSet<>();
        nodeMap = new HashMap<>();
        actIdxMap = new HashMap<>();
        isMatured = false;
        addNode(initial);

        NFANode accept = transition == EPSILON ? initial : NFANodeFactory.produceNode();
        initial.addTransition(transition, accept.getState());
        accepts.add(accept.getState());
        addNode(accept);
    }

    /**
     * Get the nfa' s initial state number
     */
    public int getInitialState() {
        return initial.getState();
    }

    /**
     * Get nfa accept states number set
     */
    public Set<Integer> getAccepts() {
        return accepts;
    }


    /**
     * Get the nodeMap of nfa.
     * The interface is aimed to open to classes in the same package
     */
    Map<Integer, NFANode> getNodeMap() {
        return nodeMap;
    }

    /**
     * Update nodeMap according to {@code nodes}
     */
    private void updateMap() {
        nodes.forEach(o -> nodeMap.put(o.getState(), o));
    }

    Set<NFANode> getNodes() {
        return nodes;
    }

    /**
     * Add a new node and update related fields
     */
    private void addNode(NFANode node) {
        if (!nodeMap.containsKey(node.getState())) {
            nodes.add(node);
            nodeMap.put(node.getState(), node);
        }
    }

    /**
     * Get the epsilon-closure in nfa by given state set
     *
     * @param source Source state set
     * @return The epsilon-closure of given set
     */
    Set<Integer> getEClosure(Set<Integer> source) {
        Set<Integer> transferred = source.parallelStream()
                .map(o -> nodeMap.get(o))
                .filter(Objects::nonNull)
                .flatMap(o -> o.transfer(EPSILON).stream())
                .collect(Collectors.toSet());

        Set<Integer> oneStepClosure = Stream.concat(source.stream(), transferred.stream()).collect(Collectors.toSet());

        if (source.size() == oneStepClosure.size())
            return source;
        else {
            //Recursive
            return getEClosure(oneStepClosure);
        }
    }

    /**
     * Link an action index with an accept state
     *
     * @param accept  target state
     * @param act_idx the given index
     * @throws FAException if the {@code accepts} does not contain the specified state
     */
    public void addActIdx(int accept, int act_idx) throws FAException {
        if (!accepts.contains(accept))
            throw new FAException("Cannot assign action to non-accept state");
        else
            actIdxMap.put(accept, act_idx);
    }

    /**
     * Get stored action index mapped by the state
     *
     * @param state key state
     * @return the action index mapped by the given state
     * @throws FAException if the state is not mapping to an action index
     */
    int getActIdx(int state) throws FAException {
        if (!actIdxMap.containsKey(state))
            throw new FAException("Isn't mapping to an action index");
        else
            return actIdxMap.get(state);

    }

    /**
     * Perform transferring on given state set and condition.
     *
     * @param condition given condition
     * @param states    nfa state set(doesn't perform if the set is fully contained by nfa state set)
     * @return the result of transferring
     */
    Set<Integer> transfer(char condition, Set<Integer> states) {
        Set<Integer> transferRes =
                states.parallelStream()
                        .map(o -> nodeMap.get(o))
                        .filter(o -> o.getConditions().contains(condition))
                        .flatMap(o -> o.transfer(condition).stream())
                        .collect(Collectors.toSet());
        return getEClosure(transferRes);
    }

    /**
     * Check if the nfa is able to be modified according to its action map.
     * In lex an nfa will gain its action after construction, hence it should not be modified after being set an action(except {@code or} combination).
     *
     * @throws FAException if attempt to modify the nfa after setting an action
     */
    private void modificationCheck() throws FAException {
        if (actIdxMap.size() != 0)
            throw new FAException("Cannot change nfa with actions set");
    }

    /**
     * Concat two nfa by connection the later one's head to former's tail and return the constructed nfa.
     *
     * @param another another nfa, will be appended to the end to this nfa
     * @return constructed nfa, also this nfa
     * @throws FAException if some nfa has been set an action, preventing modification
     */
    public NFA concat(NFA another) throws FAException {
        modificationCheck();
        another.modificationCheck();
        this.nodes.stream()
                .filter(o -> accepts.contains(o.getState()))
                .forEach(o -> {
                    o.addTransition(EPSILON, another.getInitialState());
                });
        this.nodes.addAll(another.nodes);
        this.updateMap();
        this.accepts = another.accepts;
        return this;
    }

    /**
     * 'or' combination of two nfa, the result of constructing will be returned.
     *
     * @param another another nfa
     * @return Constructed nfa, also this nfa
     */
    public NFA or(NFA another) {
        NFANode newInitial = NFANodeFactory.produceNode();
        newInitial.addTransition(EPSILON, this.getInitialState());
        newInitial.addTransition(EPSILON, another.getInitialState());
        initial = newInitial;
        addNode(initial);
        nodes.addAll(another.nodes);
        accepts.addAll(another.accepts);

        if (isMatured) {
            //Only merge nfa fields, not constructing new accept states
            actIdxMap.putAll(another.actIdxMap);
        } else {
            //During the construction process
            NFANode newAccept = NFANodeFactory.produceNode();
            addNode(newAccept);
            nodes.parallelStream()
                    .filter(o -> accepts.contains(o.getState()))
                    .forEach(o -> {
                        o.addTransition(EPSILON, newAccept.getState());
                    });
            accepts.clear();
            accepts.add(newAccept.getState());
        }
        updateMap();
        return this;
    }

    /**
     * Make this
     *
     * @return the nfa with repetition
     * @throws FAException if the nfa cannot be modified
     */
    public NFA repeat() throws FAException {
        modificationCheck();
        accepts.forEach(o -> {
            initial.addTransition(EPSILON, o);
            nodeMap.get(o).addTransition(EPSILON, initial.getState());
        });
        return this;
    }

    void mature() throws FAException {
        if (actIdxMap.isEmpty())
            throw new FAException("Haven't set an action");
        isMatured = true;
    }
}
