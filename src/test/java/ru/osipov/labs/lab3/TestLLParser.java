package ru.osipov.labs.lab3;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.mozilla.universalchardet.UniversalDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
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
import ru.osipov.labs.lab3.parsers.LRAlgorithm;
import ru.osipov.labs.lab3.parsers.LRParser;
import ru.osipov.labs.lab3.parsers.ParserMode;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestLLParser.class)
@ActiveProfiles("test")
public class TestLLParser {

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
        G = Grammar.deleteLeftRecursion(G);
        G = G.deleteLeftFactor();
        assert G != null;
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        assert lexer.getFinished().size() == 5;
        lexer.getImagefromStr(dir,"lexer_Test1");
        LLParser sa = new LLParser(G,lexer);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        System.out.println(t.toString());
        String e = "{E_1{E_1'1{E_1'1{e}}{T_2{T_2'1{e}}{F_3{a}}}{+}}{T_2{T_2'1{T_2'1{e}}{F_3{a}}{*}}{F_3{)}{E_1{E_1'1{E_1'1{e}}{T_2{T_2'1{e}}{F_3{a}}}{+}}{T_2{T_2'1{e}}{F_3{a}}}}{(}}}}";
        assert e.equals(t.toString());
        Graphviz.fromString(t.toDot("parser_test1")).render(Format.PNG).toFile(new File(dir+"parser_Test1"));
    }

    @Test
    public void test2() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\S_G_lab3.txt";
        p = p+"grammarJson\\G_Lab3_3.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        G = Grammar.deleteLeftRecursion(G);
        G = G.deleteLeftFactor();
        assert G != null;
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        assert lexer.getFinished().size() == 16;
        lexer.getImagefromStr(dir,"lexer_Test2");
        LLParser sa = new LLParser(G,lexer);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        System.out.println(t.toString());
        String e2 ="{E_1{AE_2{AE_2'1{AE_2'1{e}}{T_4{T_4'1{e}}{F_6{F_6''1{e}}{PE_8{num}}}}{PLUSOP_5{-}}}{T_4{T_4'1{e}}{F_6{F_6''1{F_6''1{e}}{PE_8{id}}{^}}{PE_8{num}}}}}{RELOP_3{==}}{AE_2{AE_2'1{AE_2'1{e}}{T_4{T_4'1{T_4'1{e}}{F_6{F_6''1{e}}{PE_8{id}}}{MULLOP_7{*}}}{F_6{F_6''1{e}}{PE_8{id}}}}{PLUSOP_5{+}}}{T_4{T_4'1{e}}{F_6{F_6''1{e}}{PE_8{id}}}}}}";
        //String e = "{E_1{AE_2{AE_2'2{AE_2'2{e}}{T_4{T_4'1{e}}{F_6{F_6'1{e}}{PE_8{num}}}}{PLUSOP_5{-}}}{T_4{T_4'1{e}}{F_6{F_6'1{F_6'1{e}}{PE_8{id}}{^}}{PE_8{num}}}}}{RELOP_3{==}}{AE_2{AE_2'2{AE_2'2{e}}{T_4{T_4'1{T_4'1{e}}{F_6{F_6'1{e}}{PE_8{id}}}{MULLOP_7{*}}}{F_6{F_6'1{e}}{PE_8{id}}}}{PLUSOP_5{+}}}{T_4{T_4'1{e}}{F_6{F_6'1{e}}{PE_8{id}}}}}}";
        assert e2.equals(t.toString());
        Graphviz.fromString(t.toDot("parser_test2")).render(Format.PNG).toFile(new File(dir+"parser_Test2"));
    }

    @Test
    public void test3() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\S_G_lab3_mod.txt";
        p = p+"grammarJson\\G_Lab3_3_m.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        G = Grammar.deleteLeftRecursion(G);
        G = G.deleteLeftFactor();
        assert G != null;
        System.out.println("Target Processed Grammar");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFA dfa = new DFA(nfa);
        dfa.deleteDeadState();
        DFALexer lexer = new DFALexer(dfa);
        assert lexer.getFinished().size() == 22;
        lexer.getImagefromStr(dir,"lexer_Test3");
        LLParser sa = new LLParser(G,lexer);
        //sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        System.out.println(t.toString());


        String e = "{P_1{}}{OPLIST_3{OPLIST_3'1{OPLIST_3'1{OPLIST_3'1{empty}}{OP_4{}}{OPLIST_3{OPLIST_3'1{empty}}{OP_4{E_5{E_5'1" +
                "{AE_6{AE_6'2{AE_6'2{empty}}{T_8{T_8'1{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{id}}}{MULLOP_11{*}}}" +
                "{F_10{F_10'1{empty}}{PE_12{id}}}}{PLUSOP_9{+}}}{T_8{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{25}}}}}{RELOP_7{<=}}}{AE_6{AE_6'2{empty}}" +
                "{T_8{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{25000000.34}}}}}}{=}{id}}}{{}}{;}}{OP_4{E_5{E_5'1{empty}}{AE_6{AE_6'2{empty}}{T_8{T_8'1{empty}}" +
                "{F_10{F_10'1{F_10'1{empty}}{PE_12{25}}{^}}{PE_12{id}}}}}}{=}{id}}{;}}{OP_4{E_5{E_5'1{AE_6{AE_6'2{empty}}{T_8{T_8'1{empty}}{F_10{F_10'1{F_10'1{empty}}" +
                "{PE_12{i}}{^}}{PE_12{25.2E-11}}}}}{RELOP_7{<>}}}{AE_6{AE_6'2{AE_6'2{empty}}{T_8{T_8'1{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{id}}}" +
                "{MULLOP_11{*}}}{F_10{F_10'1{empty}}{PE_12{id}}}}{PLUSOP_9{+}}}{T_8{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{id}}}}}}{=}{id}}}{{}}";
        assert e.equals(t.toString());
        Graphviz.fromString(t.toDot("parser_test3")).render(Format.PNG).toFile(new File(dir+"parser_Test3"));
    }

    @Test
    public void testJsonGrammar() throws IOException {

        //Detect coding of input files.
        UniversalDetector codec = new UniversalDetector(null);

        //prepare paths to files.
        String p = System.getProperty("user.dir");
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String fi1 = new String(s);
        fi1 = fi1 + "input\\json_t_1.txt";
        String fi2 = new String(s);
        fi2 = fi2 + "input\\dataset1.json";
        String fi3 = new String(s);
        fi3 = fi3 + "input\\dataset2.json";
        String fi4 = new String(s);
        fi4 = fi4 + "input\\dataset3.json";
        String fi5 = new String(s);
        fi5 = fi5 + "input\\dataset4.json";
        String fi6 = new String(s);
        fi6 = fi6 + "input\\dataset5.json";
        String fi7 = new String(s);
        fi7 = fi7 + "input\\dataset6.json";

        p = p+"grammarJson\\Json_ECMA_404.json";//grammar description (CFG)

        //build parser based on automatic model.
        SimpleJsonParser parser = new SimpleJsonParser(1000);
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        G = G.deleteLeftFactor();
        assert G != null;
        System.out.println("Processed Grammar");
        System.out.println(G);

        //build lexer
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFA dfa = new DFA(nfa);
        DFALexer lexer = new DFALexer(dfa,1000);
        lexer.setKeywords(G.getKeywords());
        //lexer.getImagefromStr(dir,"lexer_jsonG_E404");

        //build parser.
        LLParser sa = new LLParser(G, lexer);


        //Test 1
        long current = System.currentTimeMillis();
        LinkedTree<Token> t = sa.parse(fi1);
        long m1 = (System.currentTimeMillis()) - current;
        assert t != null;

        current = System.currentTimeMillis();
        JsonObject ob2 = parser.parse(fi1);
        long m2 = (System.currentTimeMillis() - current);

        System.out.println("json_t_1.txt");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2+"\n");

        //Test 2
        current = System.currentTimeMillis();
        t = sa.parse(fi4);
        m1 = (System.currentTimeMillis()) - current;
        assert t != null;
        current = System.currentTimeMillis();
        ob2 = parser.parse(fi4);//for fi4 longest str is 596 symbols
        m2 = (System.currentTimeMillis() - current);
        assert ob2 != null;

        System.out.println("dataset3.json");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2);

        //Test 3
        current = System.currentTimeMillis();
        t = sa.parse(fi5);
        m1 = (System.currentTimeMillis()) - current;
        assert t != null;
        current = System.currentTimeMillis();
        ob2 = parser.parse(fi5);
        m2 = (System.currentTimeMillis() - current);
        assert ob2 != null;

        System.out.println("dataset4.json");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2);

//        //Test 4
//        current = System.currentTimeMillis();
//        t = sa.parse(_______,fi2);
//        m1 = (System.currentTimeMillis()) - current;
//        assert t != null;
//        current = System.currentTimeMillis();
//        ob2 = parser.parse(fi2);
//        m2 = (System.currentTimeMillis() - current);
//        assert ob2 != null;
//
//        System.out.println("dataset1.json");
//        System.out.println("Gen: "+m1);
//        System.out.println("Recurse: "+m2);
//
//        //Test 5
//        current = System.currentTimeMillis();
//        t = sa.parse(_______,fi3);
//        m1 = (System.currentTimeMillis()) - current;
//        assert t != null;
//
//        current = System.currentTimeMillis();
//        ob2 = parser.parse(fi3);
//        m2 = (System.currentTimeMillis()) - current;
//        assert ob2 != null;
//
//        System.out.println("dataset2.json");
//        System.out.println("Gen: "+m1);
//        System.out.println("Recurse: "+m2);

// //       Test 6
//        current = System.currentTimeMillis();
//        t = sa.parse(_______,fi6);
//        m1 = (System.currentTimeMillis()) - current;
//        assert t != null;
//
//        current = System.currentTimeMillis();
//        ob2 = parser.parse(fi6);
//        m2 = (System.currentTimeMillis()) - current;
//        assert ob2 != null;
//
//        System.out.println("dataset5.json");
//        System.out.println("Gen: "+m1);
//        System.out.println("Recurse: "+m2);

        //Test 7
        current = System.currentTimeMillis();
        t = sa.parse(fi7);
        m1 = (System.currentTimeMillis()) - current;
        assert t != null;

        current = System.currentTimeMillis();
        ob2 = parser.parse(fi7);
        m2 = (System.currentTimeMillis()) - current;
        assert ob2 != null;

        System.out.println("dataset6.json");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2);

    }

    @Test
    void testJsonGrammar2() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String fi1 = new String(s);
        fi1 = fi1 + "input\\json_t_1.txt";
        String fi2 = new String(s);
        fi2 = fi2 + "input\\dataset1.json";
        String fi3 = new String(s);
        fi3 = fi3 + "input\\dataset2.json";
        String fi4 = new String(s);
        fi4 = fi4 + "input\\dataset3.json";
        String fi5 = new String(s);
        fi5 = fi5 + "input\\dataset4.json";
        String fi6 = new String(s);
        fi6 = fi6 + "input\\dataset5.json";
        String fi7 = new String(s);
        fi7 = fi7 + "input\\dataset6.json";

        p = p+"grammarJson\\Json_ECMA_404_2.json"; //it is LR(1) Grammar but not SLR(1) (LR(0))!
        SimpleJsonParser parser = new SimpleJsonParser(1000);
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));

        lexer.getImagefromStr(dir,"lexer_ECMA_2");
//        System.out.println("DELETE LEFT RECURSION");
//        Grammar G1 = Grammar.deleteLeftRecursion(_______);
//        System.out.println("DELETE LEFT PREFFIXES");
//        G1 = G1.deleteLeftFactor();

        //Create parser.
        LRParser sa = new LRParser(G,lexer,LRAlgorithm.SLR);
        //LLParser sa = new LLParser(G1,lexer);
        System.out.println("Parser created");

        //Test 1
        long current = System.currentTimeMillis();
        LinkedTree<Token> t = sa.parse(fi1);
        long m1 = (System.currentTimeMillis()) - current;
        assert t != null;

        current = System.currentTimeMillis();
        JsonObject ob2 = parser.parse(fi1);
        long m2 = (System.currentTimeMillis() - current);

        System.out.println("json_t_1.txt");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2+"\n");

        //Test 2
        current = System.currentTimeMillis();
        t = sa.parse(fi4);
        m1 = (System.currentTimeMillis()) - current;
        assert t != null;
        current = System.currentTimeMillis();
        ob2 = parser.parse(fi4);//for fi4 longest str is 596 symbols
        m2 = (System.currentTimeMillis() - current);
        assert ob2 != null;

        System.out.println("dataset3.json");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2);

        //Test 3
        current = System.currentTimeMillis();
        t = sa.parse(fi5);
        m1 = (System.currentTimeMillis()) - current;
        assert t != null;
        current = System.currentTimeMillis();
        ob2 = parser.parse(fi5);
        m2 = (System.currentTimeMillis() - current);
        assert ob2 != null;

        System.out.println("dataset4.json");
        System.out.println("Gen: "+m1);
        System.out.println("Recurse: "+m2);
    }
}
