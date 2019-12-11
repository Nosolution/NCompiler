import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Global {
    final static Set<Character> FULLCHARSET = new HashSet<>(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '!', '\"', '#', '%', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '[', '\\', ']', '^', '{', '|', '}', '_', ' ', '\n', '\t', '\f', '~', '&'));
    final static Set<Character> ESCAPECHARSET = new HashSet<>(Arrays.asList('.', '|', '*', '(', ')', '+', '?', '{', '}', '[', ']'));
}
