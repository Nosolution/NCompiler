package lex.fa;

/**
 * Description of class
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/12
 */
public class Tuple<U, V> {
    private final U first;
    private final V second;

    public Tuple(U first, V second) {
        this.first = first;
        this.second = second;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

}
