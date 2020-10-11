package ru.osipov.labs.Experimental;

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
import ru.osipov.labs.lab3.TestSLRParser;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.generators.FALexerGenerator;
import ru.osipov.labs.lab3.parsers.LRAlgorithm;
import ru.osipov.labs.lab3.parsers.LRParser;
import ru.osipov.labs.lab3.parsers.ParserMode;
import ru.osipov.labs.lab3.trees.LinkedTree;
import ru.osipov.labs.lab3.trees.SequentialNRVisitor;
import ru.osipov.labs.lab3.trees.VisitorMode;
import ru.osipov.labs.lab4.semantics.BreakChainNode;
import ru.osipov.labs.lab4.semantics.DeleteUselessSyntaxNode;
import ru.osipov.labs.lab4.semantics.MakeAstTree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestSLRParser.class)
public class ExperimentsTest {

    @Test
    public void testG1() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\Experimental\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\Experimental\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\Experimental\\";
        s = s + "input\\C#_Cut.json";
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
        lexer.getImagefromStr(dir,"lexer_SLR_Experiment");
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);
        sa.setParserMode(ParserMode.DEBUG);

        lexer.setIdName(null);
        LinkedTree<Token> tree = sa.parse(s);
        assert tree != null;

        System.out.println("PTree nodes = "+tree.getCount());


        tree.setVisitor(new SequentialNRVisitor<Token>());
        DeleteUselessSyntaxNode a1 = new DeleteUselessSyntaxNode(G);

        BreakChainNode a2 = new BreakChainNode();
        MakeAstTree a3 = new MakeAstTree(G);

        tree.visit(VisitorMode.PRE,a1);
        tree.visit(VisitorMode.PRE,a2);
        tree.visit(VisitorMode.PRE,a3);

        System.out.println("Processing completed");
        System.out.println("PTree nodes after = "+tree.getCount());

        //Tree is too larg for showing...
        Graphviz.fromString(tree.toDot("parser_SLR_Experiment")).render(Format.PNG).toFile(new File(dir+"parser_SLR_Experiment"));
    }

    @Test
    public void testG2() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\Experimental\\";
        String dir = System.getProperty("user.dir") +"\\src\\test\\java\\ru\\osipov\\labs\\Experimental\\";
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\Experimental\\";
        s = s + "input\\AExpr_G.json";
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
        lexer.getImagefromStr(dir,"lexer_SLR_Experiment_test2");
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);
        sa.setParserMode(ParserMode.DEBUG);

        lexer.setIdName(null);
        LinkedTree<Token> tree = sa.parse(s);
        assert tree != null;

        System.out.println("PTree nodes = "+tree.getCount());


        tree.setVisitor(new SequentialNRVisitor<Token>());
        DeleteUselessSyntaxNode a1 = new DeleteUselessSyntaxNode(G);

        BreakChainNode a2 = new BreakChainNode();
        MakeAstTree a3 = new MakeAstTree(G);

        tree.visit(VisitorMode.PRE,a1);
        tree.visit(VisitorMode.PRE,a2);
        tree.visit(VisitorMode.PRE,a3);

        System.out.println("Processing completed");
        System.out.println("PTree nodes after = "+tree.getCount());

        //Tree is too larg for showing...
        Graphviz.fromString(tree.toDot("parser_SLR_Experiment_test2")).render(Format.PNG).toFile(new File(dir+"parser_SLR_Experiment_test2"));

        //Build Grammar G (with empty fields) (GrammarMetaInfo GM has also empty fields!)
        //Call GrammarReader.readGrammar(tree,G) to fill data (also fills GrammarMetaInfo GM)
        //return modified G.
        Grammar GR = Grammar.makeGrammarFromTree(tree);
        System.out.println(GR.toString());
    }
}
