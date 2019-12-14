package lex.fa;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Map<Integer, Integer> actIdxes;

    /**
     * Construct a dfa by given nfa
     *
     * @throws FAException if something wrong occurs in finding actions
     */
    public DFA(NFA nfa) throws FAException {
        //Initialization
        nodes = new HashSet<>();
        nodeMap = new HashMap<>();
        accepts = new HashSet<>();
        actIdxes = new HashMap<>();
        Map<Set<Integer>, Integer> nodeSetMap = new HashMap<>();
        Map<Integer, NFANode> nfaNodeMap = nfa.getNodeMap();

        int nodeNumber = 0;
        Set<Integer> stateSet = new HashSet<>();
        //Prepare initial state set
        stateSet.add(nfa.getInitialState());
        stateSet = nfa.getEClosure(stateSet);
        initial = new DFANode(nodeNumber++);
        addNode(initial);
        nodeSetMap.put(stateSet, initial.getState());


        Queue<Set<Integer>> nfaStateSets = new LinkedList<>();
        nfaStateSets.offer(stateSet);

        //Loop until all stateSet is visited
        while (!nfaStateSets.isEmpty()) {
            stateSet = nfaStateSets.poll();
            //The current DFANode that will try with all possible conditions
            DFANode curNode = nodeMap.get(nodeSetMap.get(stateSet));

            //Get all possible conditions
            Set<Character> allConditions = stateSet.parallelStream()
                    .flatMap(o -> nfaNodeMap.get(o)
                            .getConditions().stream())
                    .collect(Collectors.toSet());
            Set<Integer> transferred;
            for (char condition : allConditions) {
                //Get new stateSet after transition
                transferred = nfa.transfer(condition, stateSet);

                //If the stateSet hasn't been met
                if (!nodeSetMap.containsKey(transferred)) {
                    nfaStateSets.offer(transferred);     //Add new stateSet to queue

                    //Create new node and update dfa
                    DFANode newNode = new DFANode(nodeNumber++);        //Create corresponding DFAnode
                    nodeSetMap.put(transferred, newNode.getState());    //Update nodeSetMap
                    curNode.addTransition(condition, newNode.getState());//Link with previous node
                    addNode(newNode);                                   //Add to dfa

                    //If the stateSet contains accept states, add to dfa accepts and add action
                    int act_idx = -1;
                    for (int i : nfa.getAccepts()) {
                        //find the smallest action idx which implies earliest rule
                        if (transferred.contains(i)) {
                            act_idx = (act_idx == -1 ? nfa.getActIdx(i) : Math.min(act_idx, nfa.getActIdx(i)));
                        }
                    }
                    if (act_idx != -1) {
                        accepts.add(newNode.getState());
                        actIdxes.put(newNode.getState(), act_idx);
                    }
                }

            }
        }
        nodeSetMap.put(stateSet, nodeNumber);
    }

    /**
     * Add new DFANode
     */
    private void addNode(DFANode node) {
        nodes.add(node);
        nodeMap.put(node.getState(), node);
    }

    /**
     * Get the node set of this dfa
     */
    Set<DFANode> getNodes() {
        return nodes;
    }

    /**
     * Get the action index map of this dfa
     */
    Map<Integer, Integer> getActIdxes() {
        return actIdxes;
    }

    /**
     * Get the accept state set of this dfa
     */
    public Set<Integer> getAccepts() {
        return accepts;
    }

    /**
     * Get start state of dfa
     */
    public int getInitialState() {
        return initial.getState();
    }

    /**
     * Minimize the number of dfa states
     */
    public void minimize() throws FAException {
        Set<Set<Integer>> clusterSet = new HashSet<>();
        //Initially group the states. Non-accept states are split into one group,
        //while accept states are in separate groups.
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

        //Loop until no splits
        boolean split;
        do {
            split = false;

            CLUSTER_CHECK:
            for (Set<Integer> stateCluster : clusterSet) {
                //Cannot split
                if (stateCluster.size() == 1)
                    continue;

                //Choose one state in a cluster as reference
                for (int i1 : stateCluster) {
                    Set<Integer> anotherSet = new HashSet<>();
                    DFANode node1 = nodeMap.get(i1);
                    DFANode node2;

                    //Check every other state in the cluster and add to the result set
                    // if the transferring results are not in the same state cluster
                    for (int i2 : stateCluster) {
                        if (i1 == i2)
                            continue;
                        //Have same condition number
                        if (node1.getConditions().equals(nodeMap.get(i2).getConditions())) {
                            node2 = nodeMap.get(i2);

                            Set<Character> curConditions = node1.getConditions();
                            for (char condition : curConditions) {
                                int r1 = node1.transfer(condition);
                                int r2 = node2.transfer(condition);
                                //If the result not in the same state cluster
                                if (r1 != r2 && clusterSet.stream().filter(o -> o.contains(r1)).noneMatch(o -> o.contains(r2))) {
                                    anotherSet.add(i2);
                                    break;
                                }
                            }
                        } else
                            anotherSet.add(i2);
                    }
                    if (anotherSet.size() != 0) {
                        split = true;
                        clusterSet.add(anotherSet);
                        clusterSet.add(stateCluster.parallelStream()
                                .filter(o -> !anotherSet.contains(o))
                                .collect(Collectors.toSet()));
                        break CLUSTER_CHECK;
                    }
                }
            }
        } while (split);


        //Stack that store the state cluster that need to map to one state
        Stack<Set<Integer>> toMapStk = new Stack<>();
        //Key is origin state number, value is mapped state number in minimal-state dfa
        Map<Integer, Integer> minimalStateMap = new HashMap<>();
        accepts.forEach(o -> minimalStateMap.put(o, o));

        clusterSet.stream().filter(o -> o.size() > 1).forEach(toMapStk::push);
        while (!toMapStk.empty()) {
            Set<Integer> toMap = toMapStk.peek();
            //Has been mapped
            if (toMap.stream().allMatch(minimalStateMap::containsKey)) {
                toMapStk.pop();
            } else {
                boolean delay = false;
                Set<Character> conditions = nodeMap.get(toMap.stream().findFirst().get()).getConditions();
                for (char condition : conditions) {
                    //If their transferring results haven't been mapped, delay the mapping process of current state cluster.
                    //In next iteration, it will continue with its result cluster.
                    Stream<Integer> transferRes = toMap.parallelStream()
                            .map(o -> nodeMap.get(o).transfer(condition));

                    //For all cluster, it has only two conditions to minimalStateMap:
                    // its states are all mapped to one state, or none of them is mapped.
                    if (transferRes.anyMatch(o -> !minimalStateMap.containsKey(o))) {
                        toMapStk.push(transferRes.collect(Collectors.toSet()));
                        delay = true;
                        break;
                    }
                }
                if (!delay) {
                    int minStateNum = toMap.stream().sorted().findFirst().get();
                    toMap.parallelStream().forEach(o -> minimalStateMap.put(o, minStateNum));
                    toMapStk.pop();
                }
            }
        }

        Set<Integer> minimalStates = new HashSet<>(minimalStateMap.values());
        DFANode node;
        for (int i : minimalStates) {
            node = nodeMap.get(i);
            for (char c : node.getConditions()) {
                node.updateTransition(c, minimalStateMap.get(node.transfer(c)));
            }
        }

        nodes.stream()
                .filter(o -> !minimalStates.contains(o.getState()))
                .forEach(o -> nodeMap.remove(o.getState()));
        nodes = nodes.stream().filter(o -> minimalStates.contains(o.getState())).collect(Collectors.toSet());


//        Set<Integer> minimalStateSet = clusterSet.parallelStream()
//                .map(o -> o.stream()
//                        .sorted()
//                        .findFirst()
//                        .get())  //clusterSet could not be empty
//                .collect(Collectors.toSet());
//
//        nodes = nodes.stream().filter(o -> minimalStateSet.contains(o.getState())).collect(Collectors.toSet());

    }


//    public int match(String regex) {
//        char condition;
//        DFANode cur = initial;
//        for (int i = 0; i < regex.length(); i++) {
//            condition = regex.charAt(i);
//            int dest = cur.transfer(condition);
//            if (dest == -1 || !nodeMap.containsKey(dest))
//                return -1;
//            cur = nodeMap.get(dest);
//        }
//
//        return actIdxes.getOrDefault(cur.getState(), -1);
//    }

}
