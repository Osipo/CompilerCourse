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
import ru.osipov.labs.lab3.trees.*;
import ru.osipov.labs.lab4.parsers.ShiftReduceParser;
import ru.osipov.labs.lab4.semantics.*;

import java.io.File;

public class Exe implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication.run(ru.osipov.labs.exe.Exe.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        RegexRPNParser rpn = new RegexRPNParser();
        String p = System.getProperty("user.dir");
        System.out.println("Current working dir: "+p);

        //for IDE
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
                Grammar G1 = Grammar.deleteLeftRecursion(G);
                System.out.println(G1);
                System.out.println("\n");
                System.out.println("Delete preffixes");
                G1 = G1.deleteLeftFactor();
                System.out.println(G1);


                //build lexer.
                FALexerGenerator lg = new FALexerGenerator();
                CNFA nfa = lg.buildNFA(G1);
                DFALexer lexer = new DFALexer(new DFA(nfa));
                lexer.getImagefromStr(sc.substring(0,sc.lastIndexOf('\\') + 1),"Lexer");

                //build parser.
                LLParser sa = new LLParser(G1,lexer);
                //sa.toFile(dir+"\\PTable.txt");

                LinkedTree<Token> tree = sa.parse(sc);

                if(tree != null){
                    System.out.println("Parsed successful.");
                    Graphviz.fromString(tree.toDot("ptree")).render(Format.PNG).toFile(new File(dir+"\\Tree"));

                    System.out.println("Tree nodes: "+tree.getCount());
                    //Print all id entries.
                    sa.printTable();
                    tree.setVisitor(new SequentialNRVisitor<Token>());

                    //Sem Action 1: Normalize from Stack of LL-analyzer. (In case of LL Grammar)
                    ReverseChildren<Token> semAct1 = new ReverseChildren<>();
                    tree.visit(VisitorMode.PRE,semAct1);
                    System.out.println("Reversed children from stack of LL");
                    Graphviz.fromString(tree.toDot("ptreeR")).render(Format.PNG).toFile(new File(dir+"\\ReversedTree"));

                    //Sem Action 2: Delete Nodes with empty Node (Rules A -> e)
                    RemoveEmptyNodes semAct2 = new RemoveEmptyNodes(G);
                    tree.visit(VisitorMode.PRE,semAct2);
                    System.out.println("Empty nodes are removed.");
                    Graphviz.fromString(tree.toDot("ptreeE")).render(Format.PNG).toFile(new File(dir+"\\NonETree"));

                    //Sem Action 3: Delete chain Nodes (Rules like A -> B, B -> C).
                    BreakChainNode semAct3 = new BreakChainNode();
                    tree.visit(VisitorMode.PRE,semAct3);
                    System.out.println("Chain was deleted");
                    Graphviz.fromString(tree.toDot("ptreeZ")).render(Format.PNG).toFile(new File(dir+"\\ZippedTree"));

                    //Sem Action 4: Delete Nodes created by left-recursion. (They are now useless).
                    BreakTailNode semAct4 = new BreakTailNode();
                    tree.visit(VisitorMode.PRE,semAct4);
                    System.out.println("Tail was deleted");
                    Graphviz.fromString(tree.toDot("ptreeZT")).render(Format.PNG).toFile(new File(dir+"\\UntailedTree"));



                    //Sem Action 5: Delete Non-Terminal Nodes. (Save only operators and operands).
                    MakeAstTree semAct5 = new MakeAstTree(G);
                    //ast1.translate(tree);
                    tree.visit(VisitorMode.PRE,semAct5);
                    System.out.println("ASTree was created.");
                    Graphviz.fromString(tree.toDot("ASTree")).render(Format.PNG).toFile(new File(dir+"\\ASTree"));
                    System.out.println("Tree nodes after processing: "+tree.getCount());
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
