package ru.osipov.labs.lab3;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.generators.FALexerGenerator;
import ru.osipov.labs.lab3.parsers.LLParser;
import ru.osipov.labs.lab3.parsers.SLRParser;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.File;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestSLRParser.class)
public class TestSLRParser {
    @Test
    public void test1() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\S_G_2_27.txt";
        p = p+"grammarJson\\G_2_27.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        assert lexer.getFinished().size() == 5;
        lexer.getImagefromStr(dir,"lexer_SLR_Test1");
        SLRParser sa = new SLRParser(G,lexer);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        Graphviz.fromString(t.toDot("SLR_Parser_Test1")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test1"));
    }
}
