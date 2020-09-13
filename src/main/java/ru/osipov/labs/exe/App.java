package ru.osipov.labs.exe;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.utils.RegexRPNParser;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.generators.FALexerGenerator;
import ru.osipov.labs.lab3.parsers.LLParser;
import ru.osipov.labs.lab3.parsers.LRAlgorithm;
import ru.osipov.labs.lab3.parsers.LRParser;
import ru.osipov.labs.lab3.parsers.ParserMode;
import ru.osipov.labs.lab3.trees.LinkedTree;
import ru.osipov.labs.lab3.trees.SequentialNRVisitor;
import ru.osipov.labs.lab3.trees.VisitorMode;
import ru.osipov.labs.lab4.semantics.*;
import ru.osipov.labs.lab4.translators.IntermediateCodeGenerator;
import ru.osipov.labs.lab4.translators.SemanticAnalyzer;
import ru.osipov.labs.lab4.translators.TranslatorActions;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

//MAIN CLASS FOR CMD (Console Application)
@SpringBootApplication
public class App implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication app = new SpringApplication(App.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        RegexRPNParser rpn = new RegexRPNParser();
        String p = System.getProperty("user.dir");
        System.out.println("Current working dir: "+p);

        String sc = null;
        String dir = null;
        if(args.length > 0){
            p = p + "\\"+args[0];
            SimpleJsonParser parser = new SimpleJsonParser();
            JsonObject ob = parser.parse(p);
            if (ob != null) {
                System.out.println("Json was read successfull.");
                Grammar G = null;
                try {
                    G = new Grammar(ob);
                    System.out.println(G);
                    p = System.getProperty("user.dir");

                    //build lexer.
                    FALexerGenerator lg = new FALexerGenerator();
                    CNFA nfa = lg.buildNFA(G);
                    DFALexer lexer = new DFALexer(new DFA(nfa));
                    lexer.getImagefromStr(p+"\\", "Lexer");

                    SLRGrammarParse(G,lexer,p,dir,sc);

                } catch (InvalidJsonGrammarException e) {//Json document has wrong structure.
                    System.out.println(e.getMessage());
                    System.exit(-3);
                }
            }
            else{//document is not JSON
                System.out.println("Invalid json. Json document is required!");
                System.exit(-2);
            }
        }
        else{//Grammar was not specified.
            System.out.println("Cannot build Parser without specified Grammar.");
            System.out.println("Action: Specify {filename.json} of Grammar after the name of this program");
            System.out.println("Note: you can optionally select type of Parser (LL(1), SLR(1), LR(1))");
            System.out.println("\n\t Type -ll -slr -lr after the name of Grammar file");
            System.out.println("\n\t By default: type of parser = slr");
            System.out.println("Example: java -jar CompilerCoursePr.jar MyGrammar.json");
            System.out.println("\n\t or java -jar CompilerCoursePr.jar MyGrammar.json [-slr | -ll | -lr]");
            System.exit(-1);
        }
    }

    private void LLGrammarParse(Grammar G, ILexer lexer,String p, String dir, String sc) throws IOException {

        //Parse Grammar to LL(1)
        System.out.println("Has cycles: " + G.hasCycles());
        System.out.println("\n");
        System.out.println("Delete left recursion");
        Grammar G1 = Grammar.deleteLeftRecursion(G);
        System.out.println(G1);
        System.out.println("\n");
        System.out.println("Delete preffixes");
        G1 = G1.deleteLeftFactor();
        System.out.println(G1);

        //build parser.
        LLParser sa = new LLParser(G1, lexer);
        System.out.println("Parser and Lexer was built successful.");
        System.out.println("Specify txt file for parsing");
        Scanner in = new Scanner(System.in);
        String i = in.nextLine();
        sc = p + "\\"+i;


        //Parse file.
        LinkedTree<Token> tree = sa.parse(sc);

        if(tree != null){
            System.out.println("Parsed successful.");
            Graphviz.fromString(tree.toDot("ptree")).render(Format.PNG).toFile(new File(p+"\\Tree"));

            System.out.println("Tree nodes: "+tree.getCount());

            tree.setVisitor(new SequentialNRVisitor<Token>());

            //Sem Action 1: Normalize from Stack of LL-analyzer. (In case of LL Grammar)
            ReverseChildren<Token> semAct1 = new ReverseChildren<>();
            tree.visit(VisitorMode.PRE,semAct1);
            System.out.println("Reversed children from stack of LL");
            Graphviz.fromString(tree.toDot("ptreeR")).render(Format.PNG).toFile(new File(p+"\\ReversedTree"));

            //Sem Action 2: Delete Nodes with empty Node (Rules A -> e)
            RemoveEmptyNodes semAct2 = new RemoveEmptyNodes(G);
            tree.visit(VisitorMode.PRE,semAct2);
            System.out.println("Empty nodes are removed.");
            Graphviz.fromString(tree.toDot("ptreeE")).render(Format.PNG).toFile(new File(p+"\\NonETree"));

            //Sem Action 3: Delete chain Nodes (Rules like A -> B, B -> C).
            BreakChainNode semAct3 = new BreakChainNode();
            tree.visit(VisitorMode.PRE,semAct3);
            System.out.println("Chain was deleted");
            Graphviz.fromString(tree.toDot("ptreeZ")).render(Format.PNG).toFile(new File(p+"\\ZippedTree"));

            //Sem Action 4: Delete Nodes created by left-recursion. (They are now useless).
            BreakTailNode semAct4 = new BreakTailNode();
            tree.visit(VisitorMode.PRE,semAct4);
            System.out.println("Tail was deleted");
            Graphviz.fromString(tree.toDot("ptreeZT")).render(Format.PNG).toFile(new File(p+"\\UntailedTree"));



            //Sem Action 5: Delete Non-Terminal Nodes. (Save only operators and operands).
            MakeAstTree semAct5 = new MakeAstTree(G);
            //ast1.translate(tree);
            tree.visit(VisitorMode.PRE,semAct5);
            System.out.println("ASTree was created.");
            Graphviz.fromString(tree.toDot("ASTree")).render(Format.PNG).toFile(new File(p+"\\ASTree"));
            System.out.println("Tree nodes after processing: "+tree.getCount());
        }
        else{
            System.out.println("Syntax errors detected!");
        }
    }

    private void SLRGrammarParse(Grammar G, ILexer lexer,String p, String dir, String sc) throws IOException {
        LRParser sa = new LRParser(G,lexer, LRAlgorithm.SLR);//Parser algorithm: SLR(1)

        //sa.setParserMode(ParserMode.DEBUG);
        System.out.println("SLR(1) Parser and Lexer was built successful.");
        System.out.println("Specify txt file for parsing");
        Scanner in = new Scanner(System.in);
        String i = in.nextLine();
        sc = p + "\\"+i;

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
                generator.generateCode(dir+"\\IECode.code");
        }
        else{
            System.out.println("Syntax errors detected!");
        }
    }
}
