import java.util.List;
import java.util.stream.Collectors;

/**
 * FA Transaction mapping
 *
 * @author Nosolution
 * @version 1.0
 * @since 2019/12/3
 */
public class Transaction {
    private List<Triplet<Integer, Character, Integer>> map;

    public List<Integer> move(Integer s, Character a) {
        return map.stream()
                .filter(o-> o.getFirst().equals(s) && o.getSecond() == a)
                .map(Triplet::getThird)
                .collect(Collectors.toList());
    }
}
