package lex.fa;

import java.util.*;
import java.util.stream.Collectors;

import static lex.Global.EPSILON;

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
    private Map<Integer, Integer> actIdxMap;


    private DFA() {
        initial = null;
        nodes = new HashSet<>();
        nodeMap = new HashMap<>();
        accepts = new HashSet<>();
        actIdxMap = new HashMap<>();
    }

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
        actIdxMap = new HashMap<>();
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

//        int count = 0;
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
                if (condition == EPSILON)
                    continue;
                //Get new stateSet after transition
                transferred = nfa.transfer(condition, stateSet);

                //If the stateSet hasn't been met
//                Set<Integer> finalTransferred = transferred;
//                if (nodeSetMap.keySet().stream().noneMatch(o -> setEquals(o, finalTransferred))) {
                if (!nodeSetMap.containsKey(transferred)) {
                    nfaStateSets.offer(transferred);     //Add new stateSet to queue
//                    System.out.println("New set:");
//                    for (int i : transferred.stream().sorted().collect(Collectors.toList()))
//                        System.out.print(i + " ");
//                    System.out.println();

                    //Create new node and update dfa
                    DFANode newNode = new DFANode(nodeNumber++);        //Create corresponding DFAnode
                    nodeSetMap.put(transferred, newNode.getState());    //Update nodeSetMap
                    curNode.addTransition(condition, newNode.getState());//Link with previous node
                    addNode(newNode);                                   //Add to dfa

                    //If the stateSet contains accept states, add to dfa accepts and add action
                    int actIdx = -1;
                    for (int i : nfa.getAccepts()) {
                        //find the smallest action idx which implies earliest rule
                        if (transferred.contains(i)) {
                            actIdx = (actIdx == -1 ? nfa.getActIdx(i) : Math.min(actIdx, nfa.getActIdx(i)));
                        }
                    }
                    if (actIdx != -1) {
                        accepts.add(newNode.getState());
                        actIdxMap.put(newNode.getState(), actIdx);
                    }
                } else {
                    if (!curNode.getConditions().contains(condition)) {
                        curNode.addTransition(condition, nodeSetMap.get(transferred));
                    }
                }

            }
        }
        return;
//        nodeSetMap.put(stateSet, nodeNumber);
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
    Map<Integer, Integer> getActIdxMap() {
        return actIdxMap;
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

//    /**
//     * Minimize the number of dfa states
//     */
//    public void minimize() throws FAException {
//        Set<Set<Integer>> clusterSet = new HashSet<>();
//        //Initially group the states. Non-accept states are split into one group,
//        //while accept states are in separate groups.
//        clusterSet.add(nodes.parallelStream()
//                .map(DFANode::getState)
//                .filter(o -> !accepts.contains(o))
//                .collect(Collectors.toSet()));
//        accepts.forEach(i -> clusterSet.add(new HashSet<Integer>() {
//                                                {
//                                                    add(i);
//                                                }
//                                            }
//        ));
//
//        //Loop until no splits
//        boolean split;
//        do {
//            split = false;
//
//            CLUSTER_CHECK:
//            for (Set<Integer> stateCluster : clusterSet) {
//                //Cannot split
//                if (stateCluster.size() == 1)
//                    continue;
//
//                //Choose one state in a cluster as reference
//                for (int i1 : stateCluster) {
//                    Set<Integer> anotherSet = new HashSet<>();
//                    DFANode node1 = nodeMap.get(i1);
//                    DFANode node2;
//
//                    //Check every other state in the cluster and add to the result set
//                    // if the transferring results are not in the same state cluster
//                    for (int i2 : stateCluster) {
//                        if (i1 == i2)
//                            continue;
//                        //Have same condition number
//                        if (node1.getConditions().equals(nodeMap.get(i2).getConditions())) {
//                            node2 = nodeMap.get(i2);
//
//                            Set<Character> curConditions = node1.getConditions();
//                            for (char condition : curConditions) {
//                                int r1 = node1.transfer(condition);
//                                int r2 = node2.transfer(condition);
//                                //If the result not in the same state cluster
//                                if (r1 != r2 && clusterSet.stream().filter(o -> o.contains(r1)).noneMatch(o -> o.contains(r2))) {
//                                    anotherSet.add(i2);
//                                    break;
//                                }
//                            }
//                        } else
//                            anotherSet.add(i2);
//                    }
//                    if (anotherSet.size() != 0) {
//                        split = true;
//                        clusterSet.add(anotherSet);
//                        clusterSet.add(stateCluster.parallelStream()
//                                .filter(o -> !anotherSet.contains(o))
//                                .collect(Collectors.toSet()));
//                        clusterSet.remove(stateCluster);
//                        break CLUSTER_CHECK;
//                    }
//                }
//            }
//        } while (split);
//
//
//        //Stack that store the state cluster that need to map to one state
//        Stack<Set<Integer>> toMapStk = new Stack<>();
//        //Key is origin state number, value is mapped state number in minimal-state dfa
//        Map<Integer, Integer> minimalStateMap = new HashMap<>();
//        for (Set<Integer> single : clusterSet.stream().filter(o -> o.size() == 1).collect(Collectors.toList())) {
//            for (int state : single)
//                minimalStateMap.put(state, state);
//        }
//
//        clusterSet.stream().filter(o -> o.size() > 1).forEach(toMapStk::push);
//        while (!toMapStk.empty()) {
//            Set<Integer> toMap = toMapStk.peek();
//            //Has been mapped
//            if (toMap.stream().allMatch(minimalStateMap::containsKey)) {
//                toMapStk.pop();
//            } else {
//                boolean delay = false;
//                Set<Character> conditions = nodeMap.get(toMap.stream().findFirst().get()).getConditions();
//                for (char condition : conditions) {
//                    //If their transferring results haven't been mapped, delay the mapping process of current state cluster.
//                    //In next iteration, it will continue with its result cluster.
//                    Set<Integer> transferRes = toMap.parallelStream()
//                            .map(o -> nodeMap.get(o).transfer(condition))
//                            .collect(Collectors.toSet());
//
//                    //For all cluster, it has only two conditions to minimalStateMap:
//                    // its states are all mapped to one state, or none of them is mapped.
//                    if (transferRes.stream().anyMatch(o -> !minimalStateMap.containsKey(o))) {
//                        toMapStk.push(transferRes);
//                        delay = true;
//                        break;
//                    }
//                }
//                if (!delay) {
//                    int minStateNum = toMap.stream().sorted().findFirst().get();
//                    toMap.parallelStream().forEach(o -> minimalStateMap.put(o, minStateNum));
//                    toMapStk.pop();
//                }
//            }
//        }
//
//        //minimal state set
//        Set<Integer> minimalStates = new HashSet<>(minimalStateMap.values());
//
//        DFANode node;
//        for (int i : minimalStates) {
//            node = nodeMap.get(i);
//            for (char c : node.getConditions()) {
//                int dest = node.transfer(c);
////                if (!minimalStateMap.containsKey(dest))
////                    System.out.println("no dest: " + dest);
//                int identifier = minimalStateMap.get(dest);
//                node.updateTransition(c, identifier);
//            }
//        }
//
//        nodes.stream()
//                .filter(o -> !minimalStates.contains(o.getState()))
//                .forEach(o -> nodeMap.remove(o.getState()));
//        nodes = nodes.stream().filter(o -> minimalStates.contains(o.getState())).collect(Collectors.toSet());
//
//        //map to smallest number set
//        Map<Integer, Integer> orderMap = new HashMap<>();
//        int order = 0;
//        for (int i : nodes.stream().map(DFANode::getState).sorted().collect(Collectors.toList()))
//            orderMap.put(i, order++);
//
//        accepts = accepts.stream().map(orderMap::get).collect(Collectors.toSet());
//        Map<Integer, Integer> actIdxBackup = new HashMap<>(actIdxes);
//        actIdxes.clear();
//        actIdxes.putAll(actIdxBackup.entrySet().stream()
//                .collect(Collectors.toMap(e -> orderMap.get(e.getKey()), Map.Entry::getValue)));
//        nodeMap.clear();
//        nodes.forEach(o -> {
//            o.changeState(orderMap.get(o.getState()));
//            nodeMap.put(o.getState(), o);
//        });
//
////        Set<Integer> minimalStateSet = clusterSet.parallelStream()
////                .map(o -> o.stream()
////                        .sorted()
////                        .findFirst()
////                        .get())  //clusterSet could not be empty
////                .collect(Collectors.toSet());
////
////        nodes = nodes.stream().filter(o -> minimalStateSet.contains(o.getState())).collect(Collectors.toSet());
//
//    }

    /**
     * Minimize the number of dfa states
     *
     * @return constructed dfa with minimal states
     */
    public DFA minimize() {
        Set<Set<Integer>> clusterSet = new HashSet<>();
        //Initially group the states. Non-accept states are split into one group,
        //while accept states are in separate groups.
        clusterSet.add(nodes.parallelStream()
                .map(DFANode::getState)
                .filter(o -> !accepts.contains(o))
                .collect(Collectors.toSet()));
        clusterSet.addAll(actIdxMap.values()
                .stream()
                .distinct()
                .map(o -> actIdxMap.entrySet().stream()
                        .filter(e -> e.getValue().equals(o))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet())
                )
                .collect(Collectors.toSet()));

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
                        clusterSet.remove(stateCluster);
                        break CLUSTER_CHECK;
                    }
                }
            }
        } while (split);

        DFA res = new DFA();

        Map<Integer, Integer> minimalStateMap = new HashMap<>();
        int nodeCnt = 0;
        for (Set<Integer> cluster : clusterSet) {
            assert (cluster.size() > 0);
            DFANode origin = nodeMap.get(cluster.stream().findAny().get());
            DFANode mappedOrigin = null;
            if (!minimalStateMap.containsKey(origin.getState())) {
                for (int i : cluster) {
                    minimalStateMap.put(i, nodeCnt);
                }
                mappedOrigin = new DFANode(nodeCnt++);
                res.addNode(mappedOrigin);
//                nodeCnt++;
            }
            if (mappedOrigin == null)
                mappedOrigin = res.nodeMap.get(minimalStateMap.get(origin.getState()));

            Set<Character> conditions = origin.getConditions();
            for (char condition : conditions) {
                int destState = origin.transfer(condition);
                Set<Integer> destSet = clusterSet.stream().filter(o -> o.contains(destState)).findAny().get();
                assert (destSet.size() > 0);
                DFANode mappedDest;
                if (destSet.stream().noneMatch(minimalStateMap::containsKey)) {
                    mappedDest = new DFANode(nodeCnt);
                    res.addNode(mappedDest);
                    mappedOrigin.addTransition(condition, mappedDest.getState());
                    destSet.forEach(o -> minimalStateMap.put(o, mappedDest.getState()));
                    nodeCnt++;
                } else {
                    mappedDest = res.nodeMap.get(minimalStateMap.get(destState));
                    if (!mappedOrigin.getConditions().contains(condition))
                        mappedOrigin.addTransition(condition, mappedDest.getState());
                }
            }

        }

        res.initial = res.nodeMap.get(minimalStateMap.get(initial.getState()));
        accepts.forEach(o -> res.accepts.add(minimalStateMap.get(o)));
        actIdxMap.keySet().forEach(o -> res.actIdxMap.put(minimalStateMap.get(o), actIdxMap.get(o)));

        return res;

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
