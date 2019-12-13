package lex.fa;

/**
 * Factory of NFANode, hiding the number of state
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */
public class NFANodeFactory {
    private static int cnt = 0;

    public static NFANode produceNode() {
        return new NFANode(cnt++);
    }
}
