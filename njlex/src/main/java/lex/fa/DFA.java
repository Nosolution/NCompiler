package lex.fa;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of class
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */
public class DFA {
    private DFANode initial;
    private Set<Integer> accepts;
    private Set<DFANode> nodes;
    private Map<Integer, DFANode> nodeMap;
    private Map<Integer, String> actions;

    public DFA(NFA nfa) throws FAException {
        nodes = new HashSet<>();  //initialization
        nodeMap = new HashMap<>();
        accepts = new HashSet<>();
        actions = new HashMap<>();
        Map<Set<Integer>, Integer> nodeSetMap = new HashMap<>();
        Map<Integer, NFANode> nfaNodeMap = nfa.getNodeMap();

        int nodeNumber = 0;
        Set<Integer> stateSet = new HashSet<>();
        stateSet.add(nfa.getInitialState());    //initial state set
        stateSet = nfa.getEClosure(stateSet);
        initial = new DFANode(nodeNumber++);
        addNode(initial);
        nodeSetMap.put(stateSet, initial.getState());


        Queue<Set<Integer>> nfaStateSets = new LinkedList<>();
        nfaStateSets.offer(stateSet);

        //loop until all stateSet is visited
        while (!nfaStateSets.isEmpty()) {
            stateSet = nfaStateSets.poll();
            //the current DFANode that will try with all possible conditions
            DFANode curNode = nodeMap.get(nodeSetMap.get(stateSet));

            //get all possible conditions
            Set<Character> allConditions = stateSet.parallelStream()
                    .flatMap(o -> nfaNodeMap.get(o)
                            .getConditions().stream())
                    .collect(Collectors.toSet());
            Set<Integer> transferred;
            for (char condition : allConditions) {
                //get new stateSet after transition
                transferred = nfa.transfer(condition, stateSet);

                //if the stateSet hasn't been met
                if (!nodeSetMap.containsKey(transferred)) {
                    nfaStateSets.offer(transferred);     //add new stateSet to queue

                    //create new node and update dfa
                    DFANode newNode = new DFANode(nodeNumber++);        //create corresponding DFAnode
                    nodeSetMap.put(transferred, newNode.getState());    //update nodeSetMap
                    curNode.addTransition(condition, newNode.getState());//link with previous node
                    addNode(newNode);                                   //add to dfa
                    //if the stateSet contains accept states, add to dfa accepts and add action
                    for (int i : nfa.getAccepts()) {
                        //assuming at most one accept state in each result stateSet
                        if (transferred.contains(i)) {
                            accepts.add(newNode.getState());
                            actions.put(newNode.getState(), nfa.getAction(i));
                            break;
                        }
                    }
                }

            }
        }
        nodeSetMap.put(stateSet, nodeNumber);
        initial = new DFANode(nodeNumber);

    }

    private void addNode(DFANode node) {
        nodes.add(node);
        nodeMap.put(node.getState(), node);
    }

    Set<DFANode> getNodes() {
        return nodes;
    }

    Map<Integer, String> getActions() {
        return actions;
    }

    public Set<Integer> getAccepts() {
        return accepts;
    }

    public void minimize() {
        Set<Set<Integer>> clusterSet = new HashSet<>();
        clusterSet.add(nodes.parallelStream()
                .map(DFANode::getState)
                .filter(o -> !accepts.contains(o))
                .collect(Collectors.toSet()));
        accepts.forEach(i -> clusterSet.add(new HashSet<Integer>() {
                                                {
                                                    add(i);
                                                }
                                            }
        ));

        boolean iterated = false;
        do {
            iterated = false;

            check:
            for (Set<Integer> stateCluster : clusterSet) {
                if (stateCluster.size() == 1)
                    continue;

                for (int i1 : stateCluster) {
                    Set<Integer> anotherSet = new HashSet<>();
                    DFANode node1 = nodeMap.get(i1);
                    DFANode node2;

                    for (int i2 : stateCluster) {
                        if (i1 == i2)
                            continue;
                        if (node1.getConditions().equals(nodeMap.get(i2).getConditions())) {
                            node2 = nodeMap.get(i2);

                            Set<Character> curConditions = node1.getConditions();
                            for (char condition : curConditions) {
                                int r1 = node1.transfer(condition);
                                int r2 = node2.transfer(condition);
                                if (r1 == r2 || clusterSet.stream().filter(o -> o.contains(r1)).anyMatch(o -> o.contains(r2))) {
                                } else {
                                    anotherSet.add(i2);
                                    break;
                                }
                            }
                        } else
                            anotherSet.add(i2);
                    }
                    if (anotherSet.size() != 0) {
                        iterated = true;
                        clusterSet.add(anotherSet);
                        clusterSet.add(stateCluster.parallelStream()
                                .filter(o -> !anotherSet.contains(o))
                                .collect(Collectors.toSet()));
                        break check;
                    }
                }
            }
        } while (iterated);

        Set<Integer> minimalStateSet = clusterSet.parallelStream()
                .map(o -> o.stream()
                        .sorted()
                        .findFirst()
                        .get())
                .collect(Collectors.toSet());

        nodes = nodes.stream().filter(o -> minimalStateSet.contains(o.getState())).collect(Collectors.toSet());

    }


    public String match(String regex) {
        char condition;
        DFANode cur = initial;
        for (int i = 0; i < regex.length(); i++) {
            condition = regex.charAt(i);
            int dest = cur.transfer(condition);
            if (dest == -1 || !nodeMap.containsKey(dest))
                return null;
            cur = nodeMap.get(dest);
        }

        return actions.getOrDefault(cur.getState(), null);
    }

}
