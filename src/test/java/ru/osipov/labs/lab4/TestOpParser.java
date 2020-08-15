package ru.osipov.labs.lab4;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.mozilla.universalchardet.UniversalDetector;
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
import ru.osipov.labs.lab3.trees.LinkedTree;
import ru.osipov.labs.lab4.parsers.ShiftReduceParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestOpParser.class)
public class TestOpParser {
    @Test
    public void test1() throws IOException {
        System.out.println("test1");
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
        s = s + "input\\S_G_lab3_mod.txt";
        p = p+"grammarJson\\G_Lab4_3.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        Grammar GL = Grammar.deleteLeftRecursion(G);
        GL = GL.deleteLeftFactor();
        assert GL != null;


        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        lexer.getImagefromStr(dir,"lexer_Test1");

        LLParser sa1 = new LLParser(GL,lexer);
        ShiftReduceParser sa2 = new ShiftReduceParser(G,lexer);

        //Test 1.
        long current = System.currentTimeMillis();
        LinkedTree<Token> t = sa1.parse(s);
        assert t != null;
        long m1 = (System.currentTimeMillis()) - current;

        current = System.currentTimeMillis();
        LinkedTree<Token> t2 = sa2.parse(G,s);
        assert t2 != null;
        long m2 = (System.currentTimeMillis()) - current;
        System.out.println("S_G_lab3_mod.txt");
        System.out.println("LL (Top-down): "+m1);
        System.out.println("Count of nodes: "+t.getCount());
        System.out.println("OP (Bottom-up): "+m2);
        System.out.println("Count of nodes: "+t2.getCount());

        Graphviz.fromString(t.toDot("LL_parser_test1")).render(Format.PNG).toFile(new File(dir+"LL_parser_test1"));
        Graphviz.fromString(t2.toDot("OP_parser_test1")).render(Format.PNG).toFile(new File(dir+"OP_parser_test1"));
    }

    @Test
    public void test2() throws IOException {
            String p = System.getProperty("user.dir");
            System.out.println(p);
            p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
            String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
            String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
            s = s + "input\\S_G_lab3_mod2.txt";
            p = p+"grammarJson\\G_Lab4_3.json";
            SimpleJsonParser parser = new SimpleJsonParser();
            JsonObject ob = parser.parse(p);
            assert ob != null;
            Grammar G = new Grammar(ob);
            System.out.println("Source");
            System.out.println(G);
            Grammar GL = Grammar.deleteLeftRecursion(G);
            GL = GL.deleteLeftFactor();
            assert GL != null;


            FALexerGenerator lg = new FALexerGenerator();
            CNFA nfa = lg.buildNFA(G);
            DFALexer lexer = new DFALexer(new DFA(nfa));
            //lexer.getImagefromStr(dir,"lexer_Test2");

            LLParser sa1 = new LLParser(GL,lexer);
            ShiftReduceParser sa2 = new ShiftReduceParser(G,lexer);


            long current = System.currentTimeMillis();
            LinkedTree<Token> t = sa1.parse(s);
            assert t != null;
            long m1 = (System.currentTimeMillis()) - current;

            current = System.currentTimeMillis();
            LinkedTree<Token> t2 = sa2.parse(G,s);
            assert t2 != null;
            long m2 = (System.currentTimeMillis()) - current;
            System.out.println("S_G_lab3_mod2.txt");
            System.out.println("LL (Top-down): "+m1);
            System.out.println("Count of nodes: "+t.getCount());
            System.out.println("OP (Bottom-up): "+m2);
            System.out.println("Count of nodes: "+t2.getCount());

            Graphviz.fromString(t.toDot("LL_parser_test2")).render(Format.PNG).toFile(new File(dir+"LL_parser_test2"));
            Graphviz.fromString(t2.toDot("OP_parser_test2")).render(Format.PNG).toFile(new File(dir+"OP_parser_test2"));
    }


    @Test
    public void test3() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab4\\";
        s = s + "input\\G_2_27_I.txt";
        p = p+"grammarJson\\G_2_27.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        Grammar GL = Grammar.deleteLeftRecursion(G);
        GL = GL.deleteLeftFactor();
        assert GL != null;


        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        lexer.getImagefromStr(dir,"lexer_Test3");

        LLParser sa1 = new LLParser(GL,lexer);
        ShiftReduceParser sa2 = new ShiftReduceParser(G,lexer);


        long current = System.currentTimeMillis();
        LinkedTree<Token> t = sa1.parse(s);
        assert t != null;
        long m1 = (System.currentTimeMillis()) - current;

        current = System.currentTimeMillis();
        LinkedTree<Token> t2 = sa2.parse(G,s);
        assert t2 != null;
        long m2 = (System.currentTimeMillis()) - current;
        System.out.println("G_2_27_I.txt");
        System.out.println("LL (Top-down): "+m1);
        System.out.println("Count of nodes: "+t.getCount());
        System.out.println("OP (Bottom-up): "+m2);
        System.out.println("Count of nodes: "+t2.getCount());

        Graphviz.fromString(t.toDot("LL_parser_test3")).render(Format.PNG).toFile(new File(dir+"LL_parser_test3"));
        Graphviz.fromString(t2.toDot("OP_parser_test3")).render(Format.PNG).toFile(new File(dir+"OP_parser_test3"));
    }
}
