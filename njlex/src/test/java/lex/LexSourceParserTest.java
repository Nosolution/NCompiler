package lex;

import lex.entity.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LexSourceParserTest {

    @org.junit.jupiter.api.Test
    void parseTest1() {
        String path = "F:/Project/NCompiler/test/lextest1.l";
        TreeMap<String, String> terms = new TreeMap<>();
        List<Rule> rules = new ArrayList<>();
        List<String> rawDefs = new ArrayList<>();
        List<String> localDefs = new ArrayList<>();
        List<String> userCode = new ArrayList<>();
        LexSourceParser.parse(path, terms, rules, rawDefs, localDefs, userCode);
        assertThat(terms).isEqualTo(new TreeMap<String, String>() {{
            put("a", "[aA]");
            put("b", "[bB]");
            put("c", "[cC]");
        }});

        assertThat(rules.size()).isEqualTo(1);
        List<Rule> mockRules = new ArrayList<>();
        mockRules.add(new Rule());
        mockRules.get(0).setPattern("[A-Za-z][A-Za-z0-9]*");
        mockRules.get(0).setAction("System.out.println(\"what\");\n");
        assertThat(rules).isEqualTo(mockRules);

        assertThat(rawDefs.size()).isEqualTo(1);
        assertThat(rawDefs.get(0)).isEqualTo("import java.util.List;\n");

        assertThat(localDefs.size()).isEqualTo(1);
        assertThat(localDefs.get(0)).isEqualTo("int t = 0;\n");

        assertThat(userCode).isEqualTo(new ArrayList<String>() {
            {
                add("private static void test(){\n");
                add("\t//\n");
                add("}\n");
            }
        });
    }

    @org.junit.jupiter.api.Test
    void parseTest2() {
        String path = "F:/Project/NCompiler/test/lextest2.l";
        TreeMap<String, String> terms = new TreeMap<>();
        List<Rule> rules = new ArrayList<>();
        List<String> rawDefs = new ArrayList<>();
        List<String> localDefs = new ArrayList<>();
        List<String> userCode = new ArrayList<>();
        LexSourceParser.parse(path, terms, rules, rawDefs, localDefs, userCode);
        assertThat(terms).isEqualTo(new TreeMap<String, String>() {{
            put("a", "[aA]");
            put("b", "[bB]");
            put("c", "[cC]");
        }});

        assertThat(rules.size()).isEqualTo(2);
        List<Rule> mockRules = new ArrayList<>();
        mockRules.add(new Rule());
        mockRules.get(0).setPattern("[A-Za-z][A-Za-z0-9]*");
        mockRules.get(0).setAction("System.out.println(\"what\");\n");
        mockRules.add(new Rule());
        mockRules.get(1).setPattern("\"hello\"");
        mockRules.get(1).setAction("ECHO\n");
        assertThat(rules).isEqualTo(mockRules);

        assertThat(rawDefs.size()).isEqualTo(0);
        assertThat(localDefs.size()).isEqualTo(0);

        assertThat(userCode).isEqualTo(new ArrayList<String>() {
            {
                add("private final static int testNum = 1;\n");
                add("\n");
                add("private static void test(){\n");
                add("\t//\n");
                add("}\n");
            }
        });
    }
}