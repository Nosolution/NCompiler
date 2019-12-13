package lex.fa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Non-deterministic finite automaton that can translate specific expression
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */

public class NFA {
    private NFANode initial;
    private Set<Integer> accepts;
    private Set<NFANode> nodes;
    private Map<Integer, NFANode> nodeMap;
    private Map<Integer, String> actions;
    private final static char EPSILON = '@';

    public NFA() {
        new NFA(EPSILON);
    }

    public NFA(char transition) {
        this.initial = NFANodeFactory.produceNode();
        accepts = new HashSet<>();
        nodes = new HashSet<>();
        nodeMap = new HashMap<>();
        actions = new HashMap<>();
        addNode(initial);

        NFANode accept = transition == EPSILON ? initial : NFANodeFactory.produceNode();
        initial.addTransition(transition, accept.getState());
        accepts.add(accept.getState());
        addNode(accept);
    }


    public int getInitialState() {
        return initial.getState();
    }

    public Set<Integer> getAccepts() {
        return accepts;
    }

    public Set<Integer> getStates() {
        return nodes.stream()
                .parallel()
                .map(NFANode::getState)
                .collect(Collectors.toSet());
    }

    public Set<Character> getConditions() {
        return nodes.stream()
                .parallel()
                .flatMap(o -> o.getConditions().stream())
                .collect(Collectors.toSet());
    }

    Set<Integer> getEClosure(Set<Integer> source) {
        Stream<NFANode> withECondition =
                nodes.parallelStream()
                        .filter(o -> source.contains(o.getState()))
                        .filter(o -> o.getConditions().contains(EPSILON));

        if (withECondition.count() == 0)
            return source;
        else {
            Set<Integer> closure = withECondition
                    .flatMap(o -> o.transfer(EPSILON).stream())
                    .collect(Collectors.toSet());

            return Stream.concat(source.stream(), getEClosure(closure).stream()).collect(Collectors.toSet());
        }
    }

    Set<Integer> transfer(char condition, Set<Integer> states) {
        Set<Integer> transferRes =
                nodes.parallelStream()
                        .filter(o -> !states.contains(o.getState()))
                        .filter(o -> !o.getConditions().contains(condition))
                        .flatMap(o -> o.transfer(condition).stream()).collect(Collectors.toSet());
        return getEClosure(transferRes);
    }

    public void addActions(int accept, String action) throws FAException {
        if (!accepts.contains(accept))
            throw new FAException("Cannot assign action to non-accept state");
        else
            actions.put(accept, action);
    }

    String getAction(int state) throws FAException {
        if (!actions.containsKey(state))
            throw new FAException("Not a accept state");
        else
            return actions.get(state);

    }

    private void modificationCheck() throws FAException {
        if (actions.size() != 0)
            throw new FAException("Cannot change nfa with actions set");
    }

    Set<NFANode> getNodes() {
        return nodes;
    }

    Map<Integer, NFANode> getNodeMap() {
        return nodeMap;
    }

    private void addNode(NFANode node) {
        nodes.add(node);
        if (!nodeMap.containsKey(node.getState()))
            nodeMap.put(node.getState(), node);
    }

    private void updateMap() {
        nodes.parallelStream()
                .filter(o -> !nodeMap.containsKey(o.getState()))
                .forEach(o -> nodeMap.put(o.getState(), o));
    }


    public NFA concat(NFA another) throws FAException {
        modificationCheck();
        this.nodes.stream()
                .filter(o -> accepts.contains(o.getState()))
                .forEach(o -> o.addTransition(EPSILON, another.getInitialState()));
        this.nodes.addAll(another.nodes);
        this.updateMap();
        this.accepts = another.accepts;
        return this;
    }

    public NFA or(NFA another) throws FAException {
        NFANode newInitial = NFANodeFactory.produceNode();
        newInitial.addTransition(EPSILON, this.getInitialState());
        newInitial.addTransition(EPSILON, another.getInitialState());

        initial = newInitial;
        nodes.addAll(another.nodes);
        accepts.addAll(another.accepts);
        NFANode newAccept = NFANodeFactory.produceNode();
        addNode(newAccept);
        nodes.parallelStream()
                .filter(o -> accepts.contains(o.getState()))
                .forEach(o -> o.addTransition(EPSILON, newAccept.getState()));
        updateMap();
        if (actions.size() == 0)
            accepts.clear();
        else
            actions.putAll(another.actions);
        accepts.add(newAccept.getState());
        return this;
    }

    public NFA repeat() throws FAException {
        modificationCheck();
        NFANode newInitial = NFANodeFactory.produceNode();
        newInitial.addTransition(EPSILON, getInitialState());

        NFANode newAccept = NFANodeFactory.produceNode();
        addNode(newAccept);
        nodes.parallelStream()
                .filter(o -> accepts.contains(o.getState()))
                .forEach(o -> {
                    o.addTransition(EPSILON, getInitialState());
                    o.addTransition(EPSILON, newAccept.getState());
                });
        newInitial.addTransition(EPSILON, newAccept.getState());
        accepts.clear();
        accepts.add(newAccept.getState());
        return this;
    }
}
