package ru.osipov.labs.exe;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.utils.RegexRPNParser;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.generators.FALexerGenerator;
import ru.osipov.labs.lab3.parsers.LLParser;
import ru.osipov.labs.lab3.parsers.generators.LLParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedTree;
import ru.osipov.labs.lab4.parsers.ShiftReduceParser;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Exe implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication.run(ru.osipov.labs.exe.Exe.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        RegexRPNParser rpn = new RegexRPNParser();
        String p = System.getProperty("user.dir");
        System.out.println("Current working dir: "+p);
        String sc = System.getProperty("user.dir")+"\\src\\main\\java\\ru\\osipov\\labs\\exe";
        String dir = System.getProperty("user.dir")+"\\src\\main\\java\\ru\\osipov\\labs\\exe";

        sc = sc + "\\Example.txt"; //INPUT

        p = p + "\\src\\main\\java\\ru\\osipov\\labs\\lab2\\";
        p = p + "grammars\\json\\C#_Cut.json";//Grammar.

        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        if (ob != null) {
            System.out.println("Json was read successfull.");
            Grammar G = null;
            try {
                G = new Grammar(ob);
                System.out.println(G);
                System.out.println("Has cycles: "+G.hasCycles());
                System.out.println("\n");
                System.out.println("Delete left recursion");
                G = Grammar.deleteLeftRecursion(G);
                System.out.println(G);
                System.out.println("\n");
                System.out.println("Delete preffixes");
                G = G.deleteLeftFactor();
                System.out.println(G);


                //build lexer.
                FALexerGenerator lg = new FALexerGenerator();
                CNFA nfa = lg.buildNFA(G);
                DFALexer lexer = new DFALexer(new DFA(nfa));
                lexer.getImagefromStr(sc.substring(0,sc.lastIndexOf('\\') + 1),"Lexer");

                //build parser.
                LLParser sa = new LLParser(G,lexer);

                LinkedTree<Token> tree = sa.parse(G,sc);

                if(tree != null){
                    System.out.println("Parsed successful.");
                    Graphviz.fromString(tree.toDot("ptree")).render(Format.PNG).toFile(new File(dir+"\\Tree"));
                }
                else{
                    System.out.println("Syntax errors detected!");
                }
            }
            catch (InvalidJsonGrammarException e){
                System.out.println(e.getMessage());
                System.exit(-2);
            }
        }
        else{
            System.out.println("Invalid json. Json document is required!");
            System.exit(-3);
        }
    }
}
