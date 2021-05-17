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
import ru.osipov.labs.lab3.parsers.LRAlgorithm;
import ru.osipov.labs.lab3.parsers.ParserMode;
import ru.osipov.labs.lab3.parsers.LRParser;
import ru.osipov.labs.lab3.trees.LinkedTree;
import ru.osipov.labs.lab3.trees.SequentialNRVisitor;
import ru.osipov.labs.lab3.trees.VisitorMode;
import ru.osipov.labs.lab4.semantics.BreakChainNode;
import ru.osipov.labs.lab4.semantics.DeleteUselessSyntaxNode;
import ru.osipov.labs.lab4.semantics.MakeAstTree;

import java.io.File;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestSLRParser.class)
public class TestSLRParser {

    //TEST LR(0)
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
        LRParser sa = new LRParser(G,lexer);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        Graphviz.fromString(t.toDot("SLR_Parser_Test1")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test1"));
    }

    //TEST LR(0)
    @Test
    public void test2() throws IOException{
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\SLR_Test_2.txt";
        p = p+"grammarJson\\G_Test_2.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        assert lexer.getFinished().size() == 4;
        lexer.getImagefromStr(dir,"lexer_SLR_Test2");
        LRParser sa = new LRParser(G,lexer);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        Graphviz.fromString(t.toDot("SLR_Parser_Test2")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test2"));
    }

    //Test LR(1)
    @Test
    public void test3() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\SLR_Test_3.txt";
        p = p+"grammarJson\\G_Test_Decls_3.json";
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
        lexer.getImagefromStr(dir,"lexer_SLR_Test3");
        LRParser sa = new LRParser(G, lexer);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        Graphviz.fromString(t.toDot("SLR_Parser_Test3")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test3"));
    }


    //LR(1) Grammar of strings like: c*dc*d [cdcd or dd or cdd or ccccdccccccccccccd..]
    @Test
    public void test4() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\SLR_Test_4.txt";
        p = p+"grammarJson\\G_416.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        assert lexer.getFinished().size() == 2;
        lexer.getImagefromStr(dir,"lexer_SLR_Test4");
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        Graphviz.fromString(t.toDot("SLR_Parser_Test4")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test4"));
    }

    @Test
    public void test5() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\G_LR1_Test_5.txt";
        p = p+"grammarJson\\G_LR1_Test_5.json"; //it is LR(1) Grammar but not SLR(1) (LR(0))!
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

        lexer.getImagefromStr(dir,"lexer_SLR_Test5");
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.CLR);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> t = sa.parse(s);
        assert t != null;
        Graphviz.fromString(t.toDot("SLR_Parser_Test5")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test5"));
    }

    @Test
    public void test6() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\lab3\\";
        s = s + "input\\json_t_1.txt";
        p = p+"grammarJson\\Json_ECMA_404_2.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        //assert lexer.getFinished().size() == 5;

        lexer.getImagefromStr(dir,"lexer_SLR_Test6");
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> tree = sa.parse(s);
        assert tree != null;
        Graphviz.fromString(tree.toDot("SLR_Parser_Test6")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test6"));

        tree.setVisitor(new SequentialNRVisitor<>());

        //Sem Action 1: Delete useless syntax nodes.
        DeleteUselessSyntaxNode act1 = new DeleteUselessSyntaxNode(G);
        tree.visit(VisitorMode.PRE,act1);
        System.out.println("Useless syntax node are deleted. (symbols like \",\" \";\" and etc.)");
        Graphviz.fromString(tree.toDot("ptreeA1")).render(Format.PNG).toFile(new File(dir+"UsefulTree_SLR_Test6"));

        //Sem Action 2: Delete chain Nodes (Rules like A -> B, B -> C).
        BreakChainNode act2 = new BreakChainNode();
        tree.visit(VisitorMode.PRE,act2);
        System.out.println("Chain was deleted");
        Graphviz.fromString(tree.toDot("ptreeA2")).render(Format.PNG).toFile(new File(dir+"ZippedTree_SLR_Test6"));

//        //Sem Action 3: Build AS Tree.
//        MakeAstTree act3 = new MakeAstTree(G);
//        tree.visit(VisitorMode.PRE,act3);
//        System.out.println("AST was built.");
//        Graphviz.fromString(tree.toDot("pTreeA3")).render(Format.PNG).toFile(new File(dir+"ASTree_SLR_Test6"));
//        System.out.println("Tree nodes after processing: "+tree.getCount());
    }

    @Test
    public void test7() throws IOException {
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
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        //assert lexer.getFinished().size() == 5;

        lexer.getImagefromStr(dir,"lexer_SLR_Test7");
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> tree = sa.parse(s);
        assert tree != null;
        Graphviz.fromString(tree.toDot("SLR_Parser_Test7")).render(Format.PNG).toFile(new File(dir+"SLR_Parser_Test7"));

        tree.setVisitor(new SequentialNRVisitor<>());

        //Sem Action 1: Delete useless syntax nodes.
        DeleteUselessSyntaxNode act1 = new DeleteUselessSyntaxNode(G);
        tree.visit(VisitorMode.PRE,act1);
        System.out.println("Useless syntax node are deleted. (symbols like \",\" \";\" and etc.)");
        Graphviz.fromString(tree.toDot("ptreeA1")).render(Format.PNG).toFile(new File(dir+"UsefulTree_SLR_Test7"));

        //Sem Action 2: Delete chain Nodes (Rules like A -> B, B -> C).
        BreakChainNode act2 = new BreakChainNode();
        tree.visit(VisitorMode.PRE,act2);
        System.out.println("Chain was deleted");
        Graphviz.fromString(tree.toDot("ptreeA2")).render(Format.PNG).toFile(new File(dir+"ZippedTree_SLR_Test7"));

        //Sem Action 3: Build AS Tree.
        MakeAstTree act3 = new MakeAstTree(G);
        tree.visit(VisitorMode.PRE,act3);
        System.out.println("AST was built.");
        Graphviz.fromString(tree.toDot("pTreeA3")).render(Format.PNG).toFile(new File(dir+"ASTree_SLR_Test7"));
        System.out.println("Tree nodes after processing: "+tree.getCount());
    }
}
