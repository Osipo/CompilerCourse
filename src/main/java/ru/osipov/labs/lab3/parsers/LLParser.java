package ru.osipov.labs.lab3.parsers;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab3.lexers.DFALexer;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.parsers.generators.LLParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class LLParser {
    private LLParserGenerator gen;
    private Map<Pair<String,String>, GrammarString> table;
    private ILexer lexer;

    public LLParser(Grammar G, ILexer lexer){
        gen = new LLParserGenerator();
        this.table = gen.getTable(G);
        this.lexer = lexer;
    }


    //Algorithm 4.20 with lexer module.
    public LinkedTree<Token> parse(Grammar G, String fname){
        FileInputStream f;
        boolean isParsed = true;
        try {
            f = new FileInputStream(new File(fname).getAbsolutePath());
            InputStreamReader ch = new InputStreamReader(f);
            LinkedStack<LinkedNode<Token>> S = new LinkedStack<>();
            LinkedNode<Token> root = new LinkedNode<>();
            LinkedNode<Token> EOF = new LinkedNode<>();
            EOF.setValue(new Token("$","$",'t'));
            root.setValue(new Token(G.getStart(),G.getStart(),'n'));
            root.setIdx(1);
            S.push(EOF);
            S.push(root);// Stack: S,$.
            LinkedNode<Token> X = S.top();
            Token tok = lexer.recognize(f);
            String t = tok.getName();
            String empty = G.getEmpty();
            int nidx = 1;
            while(!X.getValue().getName().equals("$")) {
                if(X.getValue().getName().equals(t)){
                    S.top().getValue().setLexem(tok.getLexem());
                    S.pop();
                    tok = lexer.recognize(f);
                    //System.out.println(tok);
                    t = tok.getName();
                    X = S.top();
                    continue;
                }
                else if(X.getValue().getName().equals("Unrecognized")){
                    System.out.println(X.getValue().getLexem());
                    isParsed = false;
                    break;
                }
                else if(G.getTerminals().contains(X.getValue().getName())) {
                    isParsed = false;
                    break;
                }
                GrammarString prod = table.get(new Pair<String,String>(X.getValue().getName(),t));
                if(prod == null) {
                    isParsed = false;
                    break;
                }
                else{
                    List<GrammarSymbol> symbols = prod.getSymbols();
                    S.pop();
                    //LinkedStack<LinkedNode<String>> RS = new LinkedStack<>();//used only to order children YK..Y1 -> Y1..Yk in brace notation
                    for(int i = symbols.size() - 1; i >= 0; i--){
                        LinkedNode<Token> node = new LinkedNode<>();
                        nidx++;
                        node.setValue(new Token(symbols.get(i).getVal(),null,symbols.get(i).getType()));
                        node.setIdx(nidx);
                        node.setParent(X);
                        X.getChildren().add(node);
                        if(!node.getValue().getName().equals(empty))
                            S.push(node);
                    }
                }
                X = S.top();
            }
            if(isParsed)
                return new LinkedTree<Token>(root);
            else {
                if(tok.getType() != 'e') {
                    lexer.generateError(S.top().getValue().getName(),tok.getLexem());
                }
                else
                    System.out.println(tok);
            }
            return null;//if syntax error return null.
        }
        catch (FileNotFoundException e){
            System.out.println("File not found. Specify file to read");
            return null;
        } catch (IOException e) {
            System.out.println("File is not available now.");
            return null;
        }
    }
}
