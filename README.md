# Compiler Generator
## Descirption
Consumes Grammar.json file which contains definition of Context Free Grammar (CFG). The structure of JSON file is given
in Section Grammar Structure.

## Build
Open cmd (or bash) go to the root of the project (directory CompilerCoursePr) and execute:
```
gradlew bootJar
```
The built application will be appeared at **./build/libs**

## Usage
The built app (jar file) is located at directory **./build/libs**  
First of all you must LOAD the **Grammar**. The **Grammar**
defines 4 elements of CFG:  **Terminals**, **NonTerminals**,  **Productions**,  **StartSymbol**.  
You can load a predefined Grammar files in follow directory  
```
/CompilerCoursePr/src/main/java/ru/osipov/labs/lab2/grammars/json
``` 
After that you must specify txt fileName for analysis and compilation.   
### Grammar Structure
The structure of json document is described as follows
>  
```JSON
{  
    "terms" : {  
          "name" : "[A-Z][A-Za-z0-9_]+",  
          "num" : "[0-9]+",  
          "empty" : null,  
          "realNum" : "[0-9]+(.[0-9]+((E|e)(@+|-)[0-9]+|empty)|empty)",  
          "character" :  "\"_\"",  
          "+" : "+",    
          "-" : "-",  
          "*" : "*",  
          "/" : "/",  
          "(" : "(",  
          ")" : ")",  
          "//" : "//",  
          "/*" : "/@*"  
    },   
    "nonTerms" : [  
        "E", "T", "S", "F"  
    ],   
    "productions" : [  
        {"S" : "E"},  
        {"E" : ["E", "+", "T"]}, {"E" : ["E", "-", "T"]}, {"E": "T"},  
        {"T" : ["T", "*", "F"]}, {"T" : ["T", "/", "F"]}, {"T" : "F"},  
        {"F" : ["(","name",")"]}, {"F" : "E"}  
    ],  
    "start" : "S",  
    "keywords" : ["true", "false", "null"],  
    "meta" : {  
        "id" : "name",  
        "commentLine" : "//",  
        "mlCommentStart" : "/*",  
        "mlCommentEnd" : "*/",  
        "scopeStart": "{",  
        "scopeEnd": "}",  
        "operands": ["name","num","realNum","true","false","character"],  
        "operators": ["+","-","*","/"],  
        "aliases": {  
          "-": "um",  
          "+": "up"  
        },  
        "types": [  
          {"name": "double","size": 8},  
          {"name": "long","size": 8},  
          {"name": "float", "size": 4},  
          {"name": "int","size": 4},  
          {"name": "char","size": 2},  
          {"name": "bool","size": 1},  
          {"name": "void","size": 0}  
        ]  
    }  
}
```

### Keywords and meta data
You can set additional metadata to your Grammar.
 1. **"keywords"** : list of strings which represent keywords of language.  
 Each ***keyword*** is treated as a terminal with same pattern as its name.  
 When you are defining **keywords** you must ensure that overlapped pattern exists.  
 Also, each character which is a part of **keyword** MUST BE IN patterns (***regular expressions***)  
 2. **"meta"** : JsonObject  which contains additional data for semantic analysis:  
    2.1 **"id"** : The name of terminal, which represent an id - entry of symbol table.  
    2.2 **"commentLine"** : The name of terminal, which represents the begining  
    of the single line comment. Comments are ignored by parser.  
    2.3 **"mlCommentStart"** : The name of terminal which represents the begining  
    of the multiline comment.  
    2.4 **"mlCommentEnd"** : The string which marks the end of the  
    multiline comment.  
    2.5 **"operands"** : The list of strings. Each element is a terminalName  
    (property name of "terms" object). This property is used for semantic Analysis.  
    2.6 **"operators"** : The list of strings. Each element is a terminalName  
    (property name of "terms" object). This property is used for semantic Analysis.  
    2.7 **"aliases"** : The alternative names for terminal symbols.  
    Used for parsing of unary operators (which has the same pattern as its binary operator)  
    2.8 **"scopeStart"** : The name of terminalName. Defines the begining of the  
    **new scope**. Used for semantic Analysis.  
    2.9 **"scopeEnd"** : The name of terminalName. Defines the end of the  
    **current scope**. Used for semantic Analysis.  
### Regex syntax
The ***regular expressions*** syntax is described as follows: 
 - `a` - *regular expression* which represent a single character string ("a").  
 (You may type a multiple character sequence `abb...`. An operator of CONCATENATION `^` will be used implicitly.)  
 (So if you type a `abb` the regex will be `a^b^b`. Of course you can use `^` operator explicitly BUT THIS IS NOT RECOMMENDED!) 
 - `r1|r2` - the UNION of two ***regular expressions (r1 and r2)***.
 - `[A-Z]` - the characters class. It is shortance for UNION operation. (i.e it is equal to expression (A|B|...|Z)    ).
 - `(r)` - the GROUPING of **regex (r1)**.
 - `_` - any single character. (The character code range is [0..65535])
 - `@` - escape symbol. (like a `\` in many programming languages).   
 Use to ignore operators (such as `^`, `|`, `(`, `)`,`*`,`+`,`_`).
 Any single character preceded with `@` is treated as an operand  
 Example: **regular expressions : `@*`, `@+`** are treated as `*` and `+`.  
 They both match strings : `"+", "*"`.  
 So to use `@` as operand you must type `@@`.
 It is unnecessary to use `@` in regexs with single character. (Expressions like `"a", "+", "*"` with length = 1)  
 Any single character expression (i.e. full expression) is treated as operand.  
 WARNING: the length of the expression is the length of the JsonString  
 (** i.e. the length of the string which is a value of property `"pattern".    
 - null - regular expression which matches **empty strings**.  

#### Regex quantifiers
There are only two quantifiers are used `*` and `+` which means:
 - `r1+` : one or more times exactly r1.
 - `r1*` : zero or more times exactly r1.

#### Regex details
After all regexs will be processed the minimal DFA will be built.  
Technically this is not formally the DFA. It is based on model  
used in **Lex, JFLex and YACC** programs.  
WARNING: All **regular expressions** are **CASE SENSITIVE**.  

There are reserved two character codes:
 - 0 - character code, which means **'_'** ,  
 regular expression which matches any single character strings.  
 - 1 - character code, which means **null**.  

You can use _terminalName_ which has **null expression**  
in other regular expressions. 


## Code Usage
See class Exe to see how to use parser in your code.  
The content of the file Exe.java is shown as follows:  
>
```
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
```

## Technical requirements.
Compiled on **JDK 1.8.0_161**.
