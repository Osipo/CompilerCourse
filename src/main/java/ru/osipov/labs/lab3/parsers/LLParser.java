package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.parsers.generators.LLParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LLParser extends Parser{
    private Map<Pair<String,String>, GrammarString> table;
    private Set<String> T;
    private Set<String> N;
    private String start;


    public LLParser(Grammar G, ILexer lexer){
        super(G,lexer);
        this.table = LLParserGenerator.getTable(G);
        this.T = G.getTerminals();
        this.N = G.getNonTerminals();
        this.start = G.getStart();
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
    public LinkedTree<Token> parse(String fname){
        try (FileInputStream f = new FileInputStream(new File(fname).getAbsolutePath())){
            LinkedStack<LinkedNode<Token>> S = new LinkedStack<>();
            LinkedNode<Token> root = new LinkedNode<>();
            LinkedNode<Token> EOF = new LinkedNode<>();
            EOF.setValue(new Token("$","$",'t'));
            root.setValue(new Token(start,start,'n'));
            root.setIdx(1);
            S.push(EOF);
            S.push(root);// Stack: S,$.
            LinkedNode<Token> X = S.top();
            Token tok = lexer.recognize(f);
            while(tok == null){
                tok = lexer.recognize(f);
            }
            String t = tok.getName();

            isParsed = true;
            int nidx = 1;//counter of elements (tree nodes)
            while(!X.getValue().getName().equals("$") && isParsed) {
                if(X.getValue().getName().equals(t)){//S.Top() == X && X == t
                    S.top().getValue().setLexem(tok.getLexem());
                    S.pop();
                    if(typeCheck)
                        if(!tok.getLexem().equals("$") && !checkScope(tok))
                            break;
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
                else if(this.T.contains(X.getValue().getName())) {
                    isParsed = false;
                    break;
                }
                GrammarString prod = table.get(new Pair<String,String>(X.getValue().getName(),t));
                if(prod == null) {
                    System.out.println("No production!!");
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
                        X.getChildren().add(node);//ON STACK: Xn..X_1 => X_1..Xn BUT ON TREE: Xn..X_1
                        if(!node.getValue().getName().equals(empty))//skip empty rules.
                            S.push(node);
                    }
                }
                X = S.top();
            }
            if(isParsed) {
                lexer.reset();//reset column and line counter.
                //isParsed = false;
                return new LinkedTree<Token>(root);
            }
            else {
                if(typeNotFound){
                    System.out.println("type with name "+tok.getLexem()+" is not defined!");
                }
                if(tok.getType() != 'e') {
                    System.out.println(lexer.generateError(S.top().getValue().getName(),tok.getName()));
                }
                else
                    System.out.println(tok);
            }
            lexer.reset();//reset column and line counter.
            return null;//if syntax error return null.
        }
        catch (FileNotFoundException e){
            lexer.reset();
            System.out.println("File not found. Specify file to read");
            return null;
        } catch (IOException e) {
            lexer.reset();
            System.out.println("File is not available now.");
            return null;
        }
    }
}
