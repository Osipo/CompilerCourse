package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.*;

import java.io.*;
import java.util.List;

public class SintPostfixTranslator implements Action<Node<Token>> {

    private STable table;
    private PositionalTree<Token> tree;
    private  FileWriter ch;
    private String idName;
    private Grammar G;
    private boolean err;
    public SintPostfixTranslator(Grammar G,STable table,PositionalTree<Token> t,String fname) throws IOException {
        this.table = table;
        this.tree = t;
        this.ch = new FileWriter(fname);
        this.err = false;
        this.idName = G.getIdName();
    }

    public boolean translate(){
        tree.visit(VisitorMode.POST, this);
        return !this.err;
    }

    @Override
    public void perform(Node<Token> arg) {
        List<Node<Token>> l = tree.getChildren(arg);
        if(l.size() == 1){//rules like A -> b or A -> C, where b = term, C = non-term
            Token t = l.get(0).getValue();
            if(t.getName().equals(idName)){
                SInfo r = table.get(t.getLexem());
                if(r != null)
                    arg.setCode(r.getValue());//N.addr = table.get(id.lexeme)
                else{
                    this.err = true;
                    System.out.println("Undefined variable "+t.getLexem());
                    arg.setCode(t.getLexem());
                }
            }
            else
                arg.setCode(t.getLexem());//N.addr = lexeme.
        }
        else if(l.size() == 3){
            Token t1 = l.get(0).getValue();
            Token t2 = l.get(1).getValue();
            Token t3 = l.get(2).getValue();
            if(t1.getName().equals("(")
                && t3.getName().equals(")")
            ){//rule like A -> ( B ).
                arg.setCode(tree.getChildren(arg).get(1).getCode());
            }
            else if(t1.getName().equals(idName) && t2.getName().equals("=")){//rule like id = E.
                String id = table.get(t1.getLexem()).getValue();
                gen(id+" "+t2.getLexem()+" "+l.get(2).getCode());
            }
        }
    }

    //Write one command in line to output file.
    private void gen(String code){
        try {
            ch.write(code);
        }catch (IOException e){
            System.out.println("Error. File is not available now!");
            this.err = true;
        }
    }
}
