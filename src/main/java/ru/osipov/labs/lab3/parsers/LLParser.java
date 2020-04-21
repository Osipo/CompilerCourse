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
    //Lexer parts.
    private char[] buf;
    private int bsize;
    private int bufp;
    private int EOL;

    private int line;
    private int col;
    private ILexer lexer;

    public LLParser(Grammar G, ILexer lexer){
        gen = new LLParserGenerator();
        this.table = gen.getTable(G);
        this.lexer = lexer;

        //Lexer parts.
        this.buf = new char[100];
        this.bsize = 99;
        this.bufp = 0;
        this.EOL = 0;
        this.line = 1;
        this.col = 0;
    }

    //Algorithm 4.20
    public LinkedTree<String> parse(Grammar G, String fname){
        FileInputStream f;
        boolean isParsed = true;
        try {
            f = new FileInputStream(new File(fname).getAbsolutePath());
            InputStreamReader ch = new InputStreamReader(f);
            LinkedStack<LinkedNode<String>> S = new LinkedStack<>();
            LinkedNode<String> root = new LinkedNode<>();
            LinkedNode<String> EOF = new LinkedNode<>();
            EOF.setValue("$");
            root.setValue(G.getStart());
            root.setIdx(1);
            S.push(EOF);
            S.push(root);// Stack: S,$.
            LinkedNode<String> X = S.top();
            String t = readLexem(f);
            String empty = G.getEmpty();
            int nidx = 1;
            while(!X.getValue().equals("$")) {
//                for(LinkedNode n : S){
//                    System.out.print(n.getValue()+" ");
//                }
//                System.out.println("");
//                System.out.println("Current: "+X.getValue()+": "+t);
                if(X.getValue().equals(t)){
                    S.pop();
                    t = readLexem(f);
                    X = S.top();
                    continue;
                }
                else if(G.getTerminals().contains(X.getValue())) {
                    isParsed = false;
                    break;
                }
                GrammarString prod = table.get(new Pair<String,String>(X.getValue(),t));
                if(prod == null) {
                    isParsed = false;
                    break;
                }
                else{
                    List<GrammarSymbol> symbols = prod.getSymbols();
                    S.pop();
                    //LinkedStack<LinkedNode<String>> RS = new LinkedStack<>();//used only to order children YK..Y1 -> Y1..Yk in brace notation
                    for(int i = symbols.size() - 1; i >= 0; i--){
                        LinkedNode<String> node = new LinkedNode<>();
                        nidx++;
                        node.setValue(symbols.get(i).getVal());
                        node.setIdx(nidx);
                        node.setParent(X);
                        //RS.push(node);
                        X.getChildren().add(node);
                        if(!node.getValue().equals(empty))
                            S.push(node);
                    }
//                    while(!RS.isEmpty()){
//                        X.getChildren().add(RS.top());
//                        RS.pop();
//                    }

                }
                X = S.top();
            }
            if(isParsed)
                return new LinkedTree<String>(root);
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

    //Algorithm 4.20 with lexer module.
    public LinkedTree<Token> parse2(Grammar G, String fname){
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
            else
                System.out.println(tok);
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

    //Lexer Part.
    //Read single terminal.
    private String readLexem(InputStream f) throws IOException{
        char cur;
        while ((cur = (char) getch(f)) == ' ' || cur == '\t' || cur == '\n' || cur == '\r') {
            if (cur == '\n') {
                line++;
                col = 0;
            }
        }
        if(((int)cur) == 65535)
            return "$";
        String t = readId(f,cur);
        return t;
    }

    //Lexer parts.
    //Read id. (term)
    private String readId(InputStream f, char c) throws IOException {
        StringBuilder b = new StringBuilder();
        int i = 0;
        b.append(c);
        while(i < 2147483647){
            c = (char)getch(f);
            if(c == '\t' || c =='\n' || c == ' ' || c == '\r' || ((int) c) == 65535)
                break;
            b.append(c);
            i++;
        }
        String v = b.toString();
        ungetch(c);
        return v;
    }

    private int ungetch(char c){
        if(bufp > bsize){
            System.out.println("Error ("+line+":"+col+"). ungetch: too many characters.");
            return 0;
        }
        else{
            buf[bufp++] = c;
            return 1;
        }
    }

    private int getch(InputStream r) throws IOException {
        if(bufp > 0)
            return buf[--bufp];
        else{
            col++;
            char c = (char)r.read();
            if(c == '\n'){
                line++; col = 0;
            }
            return c;
        }
    }

    private int getFilech(InputStream r) throws IOException {
        char c = (char)r.read();
        if(c == '\n'){
            line += 1;
            col = 0;
        }
        else{
            col += 1;
        }
        return c;
    }

    private void clear(){
        EOL = 0;
        bufp = 0;
        this.buf = null;
        this.buf = new char[bsize];
    }
}
