package ru.osipov.labs.xml;

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
import ru.osipov.labs.lab3.parsers.LRParser;
import ru.osipov.labs.lab3.parsers.ParserMode;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.File;
import java.io.IOException;


@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestXml.class)
public class TestXml {
    @Test
    public void testXml() throws IOException {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p + "\\src\\test\\java\\ru\\osipov\\labs\\xml\\";
        String dir = System.getProperty("user.dir") + "\\src\\test\\java\\ru\\osipov\\labs\\xml\\";
        String s = System.getProperty("user.dir") + "\\src\\test\\java\\ru\\osipov\\labs\\xml\\";
        s = s + "input\\pseudo_xml4.xml";
        p = p + "grammars\\Xml_4th.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        FALexerGenerator lg = new FALexerGenerator();
        CNFA nfa = lg.buildNFA(G);
        DFALexer lexer = new DFALexer(new DFA(nfa));
        lexer.getImagefromStr(dir,"Xml_lexer_testXml");

        LRParser sa = new LRParser(G, lexer, LRAlgorithm.SLR);
        sa.setParserMode(ParserMode.DEBUG);
        LinkedTree<Token> tree = sa.parse(s);
        assert tree != null;

        Graphviz.fromString(tree.toDot("SLR_Parser_Test1")).render(Format.PNG).toFile(new File(dir+"Xml_parser_test"));
    }
}
