package lex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void main() {
        Main.main(new String[]{"lex", "F:/Project/NCompiler/test/lextest5.l"});
    }
}