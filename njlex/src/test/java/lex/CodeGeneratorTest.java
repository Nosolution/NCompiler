package lex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CodeGeneratorTest {

    @Test
    void generateTest1() {
        CodeGenerator generator = new CodeGenerator("F:/Project/NCompiler/Lex.yy.java");
        List<int[]> tables = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            tables.add(new int[0]);
        List<String> actions = new ArrayList<>();
        int startState = 0;
        List<String> rawDefs = new ArrayList<>();
        List<String> localDefs = new ArrayList<>();
        List<String> userCode = new ArrayList<>();
        try {
            generator.generate(tables, actions, startState, rawDefs, localDefs, userCode);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}