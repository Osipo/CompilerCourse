package ru.osipov.labs.exe;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.generators.FALexerGenerator;
import ru.osipov.labs.lab3.parsers.LRAlgorithm;
import ru.osipov.labs.lab3.parsers.ParserMode;
import ru.osipov.labs.lab3.parsers.LRParser;
import ru.osipov.labs.lab3.trees.*;
import ru.osipov.labs.lab4.semantics.*;
import ru.osipov.labs.lab4.translators.IntermediateCodeGenerator;
import ru.osipov.labs.lab4.translators.SemanticAnalyzer;
import ru.osipov.labs.lab4.translators.TranslatorActions;

import java.io.File;
import java.io.IOException;

public class Exe implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication.run(ru.osipov.labs.exe.Exe.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        String p = System.getProperty("user.dir");
        System.out.println("Current working dir: "+p);

        //for IDE
        String sc = System.getProperty("user.dir")+"\\src\\main\\java\\ru\\osipov\\labs\\exe";
        String dir = System.getProperty("user.dir")+"\\src\\main\\java\\ru\\osipov\\labs\\exe";

        sc = sc + "\\Example_2.txt"; //INPUT

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

                //build lexer.
                FALexerGenerator lg = new FALexerGenerator();
                CNFA nfa = lg.buildNFA(G);
                DFALexer lexer = new DFALexer(new DFA(nfa));
                lexer.getImagefromStr(sc.substring(0,sc.lastIndexOf('\\') + 1),"Lexer");


                //Parse grammar which type is SLR or LR(0)
                SLRGrammarParse(G,lexer,dir,sc);
            }
            catch (InvalidJsonGrammarException e){
                System.out.println(e.getMessage());
                System.exit(-2);
            }
            catch (IOException e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Invalid json. Json document is required!");
            System.exit(-3);
        }
    }

    private void SLRGrammarParse(Grammar G, ILexer lexer,String dir,String sc) throws IOException {
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);//Parser algorithm: SLR(1)
        sa.setParserMode(ParserMode.DEBUG);
        System.out.println("\n");
        LinkedTree<Token> tree = sa.parse(sc);
        if(tree != null){
            System.out.println("Parsed successful.");
            Graphviz.fromString(tree.toDot("ptree")).render(Format.PNG).toFile(new File(dir+"\\Tree_SLR"));

            System.out.println("Tree nodes: "+tree.getCount());

            tree.setVisitor(new SequentialNRVisitor<Token>());

            //Sem Action 1: Delete useless syntax nodes.
            DeleteUselessSyntaxNode act1 = new DeleteUselessSyntaxNode(G);
            tree.visit(VisitorMode.PRE,act1);
            System.out.println("Useless syntax node are deleted. (symbols like \",\" \";\" and etc.)");
            Graphviz.fromString(tree.toDot("ptreeA1")).render(Format.PNG).toFile(new File(dir+"\\UsefulTree_SLR_a1"));

            //Sem Action 2: Delete chain Nodes (Rules like A -> B, B -> C).
            BreakChainNode act2 = new BreakChainNode();
            tree.visit(VisitorMode.PRE,act2);
            System.out.println("Chain was deleted");
            Graphviz.fromString(tree.toDot("ptreeA2")).render(Format.PNG).toFile(new File(dir+"\\ZippedTree_SLR_a2"));

            //Sem Action 3: Build AS Tree.
            MakeAstTree act3 = new MakeAstTree(G);
            tree.visit(VisitorMode.PRE,act3);
            System.out.println("AST was built.");
            Graphviz.fromString(tree.toDot("pTreeA3")).render(Format.PNG).toFile(new File(dir+"\\ASTree_SLR"));
            System.out.println("Tree nodes after processing: "+tree.getCount());

            //Semantic analyzer and IE code generator.
            SemanticAnalyzer semantic = new SemanticAnalyzer(G,tree);

            tree.visit(VisitorMode.PRE,semantic);//SEARCH_DEFINITIONS
            System.out.println("Definitions are read.");

            semantic.setActionType(TranslatorActions.RENAME_PARAMS);
            semantic.reset();
            tree.visit(VisitorMode.PRE,semantic);
            System.out.println("Parameters of methods in bodies are renamed.");

            semantic.setActionType(TranslatorActions.TYPE_CHECK);
            semantic.reset();
            tree.visit(VisitorMode.PRE,semantic);
            System.out.println("Types were checked.");

            IntermediateCodeGenerator generator = semantic.getCodeGenerator();
            System.out.println("Has errors: "+semantic.hasErrors());
            if(semantic.hasErrors())
                semantic.showErrors();
            else
                generator.generateCode(sc.substring(0,sc.lastIndexOf('.'))+"IE.code");
        }
        else{
            System.out.println("Syntax errors detected!");
        }
    }
}
