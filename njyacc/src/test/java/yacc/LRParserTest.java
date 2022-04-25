package yacc;

import org.junit.jupiter.api.Test;

class LRParserTest {

    @Test
    void mainTest1() {
        LRParser.main(new String[]{"example1.txt"});
    }

    @Test
    void mainTest2() {
        LRParser.main(new String[]{"F:/Project/NCompiler/test/yacctest/example2.txt"});
    }

    @Test
    void mainTest3() {
        LRParser.main(new String[]{"F:/Project/NCompiler/test/yacctest/example3.txt"});
    }
}