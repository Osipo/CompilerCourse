package ru.osipov.labs.lab3;

import guru.nidi.graphviz.engine.Format;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import ru.osipov.labs.lab1.structures.graphs.Graph;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.parsers.LLParser;
import ru.osipov.labs.lab3.parsers.generators.LLParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedTree;
import guru.nidi.graphviz.engine.Graphviz;

import java.io.File;
import java.util.*;

@SpringBootConfiguration
public class Lab3 implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication.run(ru.osipov.labs.lab3.Lab3.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        String p = System.getProperty("user.dir");
        System.out.println("Current working dir: "+p);
        String sc = System.getProperty("user.dir")+"\\src\\main\\java\\ru\\osipov\\labs\\lab3";
        sc = sc + "\\S_G_lab3.txt";
        p = p + "\\src\\main\\java\\ru\\osipov\\labs\\lab2\\";
        p = p + "grammars\\json\\G_Lab3_3.json";

        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        if (ob != null) {
            System.out.println("Json was read successfull.");
            Grammar G = null;
            try {
                G = new Grammar(ob);
            }
            catch (InvalidJsonGrammarException e){
                System.out.println(e.getMessage());
                System.exit(-2);
            }
            System.out.println(G);
            System.out.println("Delete left recursion");
            G = Grammar.deleteLeftRecursion(G);
            System.out.println(G);
            System.out.println("Delete preffixes");
            G = G.deleteLeftFactor();
            System.out.println(G);
            LLParserGenerator gen = new LLParserGenerator();
            System.out.println("Build FIRST");
            System.out.println("--------------");
            Map<String,Set<String>> FIRST = gen.firstTable(G);
            for(String k : FIRST.keySet()){
                System.out.println(k+": "+FIRST.get(k));
            }
            System.out.println("--------------");
            System.out.println("Build FOLLOW");
            System.out.println("--------------");
            Map<String,Set<String>> FOLLOW = gen.followTable(G,FIRST);
            for(String k : FOLLOW.keySet()){
                System.out.println(k+": "+FOLLOW.get(k));
            }
            System.out.println("-------------");
            LLParser syntaxP = new LLParser(G);
            System.out.println("Get Tree of G");
            LinkedTree<String> tree = syntaxP.parse(G,sc);
            System.out.println("--------");
            if(tree != null) {
                System.out.println(tree);
                System.out.println(tree.toDot("tr"));
                Graphviz.fromString(tree.toDot("tr")).render(Format.PNG).toFile(new File(sc.substring(0,sc.lastIndexOf('\\') + 1)+"tr"));
            }
        }
        else{
            System.out.println("Invalid json. Json document is required!");
            System.exit(-3);
        }
    }
}