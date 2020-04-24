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
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.File;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestLLParser.class)
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
        LinkedTree<Token> t = sa.parse(G,s);
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
        LinkedTree<Token> t = sa.parse(G,s);
        assert t != null;
        System.out.println(t.toString());
        String e = "{E_1{AE_2{AE_2'2{AE_2'2{e}}{T_4{T_4'1{e}}{F_6{F_6'1{e}}{PE_8{num}}}}{PLUSOP_5{-}}}{T_4{T_4'1{e}}{F_6{F_6'1{F_6'1{e}}{PE_8{id}}{^}}{PE_8{num}}}}}{RELOP_3{==}}{AE_2{AE_2'2{AE_2'2{e}}{T_4{T_4'1{T_4'1{e}}{F_6{F_6'1{e}}{PE_8{id}}}{MULLOP_7{*}}}{F_6{F_6'1{e}}{PE_8{id}}}}{PLUSOP_5{+}}}{T_4{T_4'1{e}}{F_6{F_6'1{e}}{PE_8{id}}}}}}";
        assert e.equals(t.toString());
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
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        assert lexer.getFinished().size() == 22;
        lexer.getImagefromStr(dir,"lexer_Test3");
        LLParser sa = new LLParser(G,lexer);
        LinkedTree<Token> t = sa.parse(G,s);
        assert t != null;
        System.out.println(t.toString());
        String e = "{P_1{}}{OPLIST_3{OPLIST_3'1{OPLIST_3'1{OPLIST_3'1{empty}}{OP_4{}}{OPLIST_3{OPLIST_3'1{empty}}{OP_4{E_5{E_5'1" +
                "{AE_6{AE_6'2{AE_6'2{empty}}{T_8{T_8'1{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{id}}}{MULLOP_11{*}}}" +
                "{F_10{F_10'1{empty}}{PE_12{id}}}}{PLUSOP_9{+}}}{T_8{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{25}}}}}{RELOP_7{<=}}}{AE_6{AE_6'2{empty}}" +
                "{T_8{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{25.34}}}}}}{=}{id}}}{{}}{;}}{OP_4{E_5{E_5'1{empty}}{AE_6{AE_6'2{empty}}{T_8{T_8'1{empty}}" +
                "{F_10{F_10'1{F_10'1{empty}}{PE_12{25}}{^}}{PE_12{id}}}}}}{=}{id}}{;}}{OP_4{E_5{E_5'1{AE_6{AE_6'2{empty}}{T_8{T_8'1{empty}}{F_10{F_10'1{F_10'1{empty}}" +
                "{PE_12{i}}{^}}{PE_12{25.2E-11}}}}}{RELOP_7{<>}}}{AE_6{AE_6'2{AE_6'2{empty}}{T_8{T_8'1{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{id}}}" +
                "{MULLOP_11{*}}}{F_10{F_10'1{empty}}{PE_12{id}}}}{PLUSOP_9{+}}}{T_8{T_8'1{empty}}{F_10{F_10'1{empty}}{PE_12{id}}}}}}{=}{id}}}{{}}";
        assert e.equals(t.toString());
        Graphviz.fromString(t.toDot("parser_test3")).render(Format.PNG).toFile(new File(dir+"parser_Test3"));
    }
}
