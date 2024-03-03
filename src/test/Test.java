package test;

import org.antlr.v4.runtime.*;

public class Test {
    public static void main(String[] args) throws Exception {
    			
        String fileName = "prova.txt";

        CharStream chars = CharStreams.fromFileName(fileName);
        TestLexer lexer = new TestLexer(chars);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TestParser parser = new TestParser(tokens);
                
    }
}
