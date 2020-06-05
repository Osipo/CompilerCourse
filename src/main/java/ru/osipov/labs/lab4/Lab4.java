package ru.osipov.labs.lab4;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;

import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.utils.RegexRPNParser;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.generators.FALexerGenerator;
import ru.osipov.labs.lab3.parsers.LLParser;
import ru.osipov.labs.lab3.parsers.generators.LLParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedTree;
import guru.nidi.graphviz.engine.Graphviz;

import java.io.File;
import java.util.*;


@SpringBootConfiguration
public class Lab4 implements CommandLineRunner {

    public static void main(String[] args){
        SpringApplication.run(ru.osipov.labs.lab4.Lab4.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        RegexRPNParser rpn = new RegexRPNParser();
        String p = System.getProperty("user.dir");

        System.out.println("Current working dir: "+p);
        String sc = System.getProperty("user.dir")+"\\src\\main\\java\\ru\\osipov\\labs\\lab4";
        sc = sc + "\\S_G_lab4_mod.txt"; //INPUT
        p = p + "\\src\\main\\java\\ru\\osipov\\labs\\lab2\\";
        p = p + "grammars\\json\\G_Lab4_3.json";//Grammar.
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        if (ob != null) {
            System.out.println("Json was read successfull.");
            Grammar G = null;
            try {
                G = new Grammar(ob);
                System.out.println(G);
                System.out.println("\n");

                Map<String,Set<GrammarSymbol>> Lmap = G.getLMap();
                System.out.println("L(U) for each non-term U was computed.");
                Map<String,Set<GrammarSymbol>> Rmap = G.getRMap();
                System.out.println("R(U) for each non-term U was computed.\n");

//                System.out.println("L(U)");
//                for(String k: Lmap.keySet()){
//                    System.out.println(k+": "+Lmap.get(k));
//                }

                Map<String,Set<String>> LTM = G.getLeftTermMap(Lmap);
                System.out.println("\nComputed Lt(U) for each non-term U");
                System.out.println("Lt(U)");
                for(String k: LTM.keySet()){
                    System.out.println(k+": "+LTM.get(k));
                }

                Map<String,Set<String>> RTM = G.getRightTermMap(Rmap);
                System.out.println("\nComputed Rt(U) for each non-term U");
                System.out.println("Rt(U)");
                for(String k: RTM.keySet()){
                    System.out.println(k+": "+RTM.get(k));
                }

//                System.out.println("\nR(U)");
//                for(String k : Rmap.keySet()){
//                    System.out.println(k+": "+Rmap.get(k));
//                }
                boolean opG = G.isOperatorGrammar();
                System.out.println("Is Operator G: "+opG);
                Grammar GS = null;
                if(opG){
                    System.out.println("Get spanning Grammar");
                    GS = G.getSpanningGrammar();
                    System.out.println(GS);
                }

                //build lexer.
                FALexerGenerator lg = new FALexerGenerator();
                CNFA nfa = lg.buildNFA(G);
                DFALexer lexer = new DFALexer(new DFA(nfa));
                lexer.getImagefromStr(sc.substring(0,sc.lastIndexOf('\\') + 1),"Lexer");


                //build parser.
                //LLParser syntaxP = new LLParser(G,lexer);
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
