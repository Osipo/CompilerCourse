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
import java.util.Set;


public class LLParser {
    private LLParserGenerator gen;
    private Map<Pair<String,String>, GrammarString> table;
    private ILexer lexer;
    private Set<String> T;
    private Set<String> N;


    public LLParser(Grammar G, ILexer lexer){
        gen = new LLParserGenerator();
        this.table = gen.getTable(G);
        this.lexer = lexer;
        lexer.setKeywords(G.getKeywords());
        lexer.setOperands(G.getOperands());
        lexer.setAliases(G.getAliases());
        lexer.setCommentLine(G.getCommentLine());
        lexer.setMlCommentStart(G.getMlCommentStart());
        lexer.setMlCommentEnd(G.getMlCommentEnd());
        lexer.setIdName(G.getIdName());
        this.T = G.getTerminals();
        this.N = G.getNonTerminals();
    }

    //TODO: make full table with empty cells (error records)
    public void toFile(String fname){
        File f = new File(fname);
        if(f.lastModified() != 0){
            System.out.println("Cannot write to existing file!");
            return;
        }
        try (FileWriter fw = new FileWriter(f,true);) {
            fw.write("{\n\t");
            int l = 0;
            for(Pair<String,String> cell : table.keySet()){
                fw.write("\""+cell.getV1()+" "+cell.getV2()+"\": ");
                GrammarString s = table.get(cell);
                if(s != null){
                    fw.write('[');
                    int syms = 0;
                    for(GrammarSymbol sym : s.getSymbols()){
                        fw.write("\""+sym.getVal()+"\"");
                        syms++;
                        if(syms != s.getSymbols().size()){
                            fw.write(", ");
                        }
                    }
                    fw.write(']');
                }
                else
                    fw.write("error");
                l++;
                if(l != table.keySet().size()){
                    fw.write(",\n\t");
                }
            }
            fw.write("\n}");
        }
        catch (FileNotFoundException e){
            System.out.println("Cannot open file to write.");
        }
        catch (IOException e){
            System.out.println("Cannot write to file");
        }
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
            while(tok == null){
                tok = lexer.recognize(f);
            }
            String t = tok.getName();
            String empty = G.getEmpty();
            int nidx = 1;
            while(!X.getValue().getName().equals("$")) {
                if(X.getValue().getName().equals(t)){//S.Top() == X && X == t.
                    S.top().getValue().setLexem(tok.getLexem());
                    S.pop();
                    tok = lexer.recognize(f);
                    while(tok == null){
                        tok = lexer.recognize(f);
                    }
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
            if(isParsed) {
                lexer.reset();//reset column and line counter.
                return new LinkedTree<Token>(root);
            }
            else {
                if(tok.getType() != 'e') {
                    System.out.println(lexer.generateError(S.top().getValue().getName(),tok.getLexem()));
                }
                else
                    System.out.println(tok);
            }
            lexer.reset();//reset column and line counter.
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
