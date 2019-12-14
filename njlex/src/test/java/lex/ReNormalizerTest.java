package lex;

import lex.entity.Rule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class ReNormalizerTest {

    @Test
    void normalizeTest1() {
        TreeMap<String, String> terms = new TreeMap<String, String>() {{
            put("a", "[aA]");
            put("b", "[bB]");
            put("c", "[cC]");
        }};

        List<Rule> mockRules = new ArrayList<>();
        mockRules.add(new Rule());
        mockRules.get(0).setPattern("[A-Za-z][A-Za-z0-9]*");
        mockRules.get(0).setAction("System.out.println(\"what\");\n");




    }
}