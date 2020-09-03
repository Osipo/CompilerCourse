package ru.osipov.labs.lab4.translators;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.TokenAttrs;
import ru.osipov.labs.lab3.trees.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class IntermediateCodeGenerator implements Action<Node<Token>> {
    private LinkedTree<Token> tree;
    private Grammar G;
    private long lcounter;
    private FileWriter writer;
    private LinkedStack<String> labels;

    public IntermediateCodeGenerator(Grammar G,LinkedTree<Token> tree,long lc){
        this.G = G;
        this.tree = tree;
        this.lcounter = lc;
        this.writer = null;
        this.labels = new LinkedStack<>();
    }

    //GEN_CODE(TREE)
    public void generateCode(String fname) throws IOException {
        File f = new File(fname);
        if(f.lastModified() != 0){
            System.out.println("Cannot write to existing file!");
            return;
        }
        FileWriter fw = null;
        try{
            fw = new FileWriter(f,true);
            this.writer = fw;
            tree.visit(VisitorMode.PRE, this);
        }
        catch (FileNotFoundException e){
            System.out.println("Cannot open file to write. File not found.");
        }
        catch (IOException e){
            System.out.println("Cannot write to file.");
        }
        finally {
            if(fw != null)
                fw.close();
        }
    }

    @Override
    public void perform(Node<Token> n) {
        LinkedNode<Token> arg = (LinkedNode<Token>) n;
        try {
            if (arg.getValue().getName().equals("def")) {
                LinkedNode<Token> m = arg.getChildren().get(1);
                writer.write(m.getValue().getLexem() + ": \n");
                tree.visit(VisitorMode.POST, this::produceCode);
            }
        }catch (IOException e){
            System.out.println("Cannot write to file.");
        }
    }

    //produce code for IF-ELSE and WHILE nodes.
    private void produceCode(Node<Token> n){
        LinkedNode<Token> arg = (LinkedNode<Token>) n;
        try {
            //IF condition was read.
            if (arg.getParent().getValue().getName().equals("if") && arg.getParent().getChildren().indexOf(arg) == 2) {
                Token if_node = arg.getParent().getValue();
                if (if_node instanceof TokenAttrs) {
                    TokenAttrs cod = (TokenAttrs) if_node;
                    StringBuilder sb = new StringBuilder();
                    lcounter++;
                    //ifFalse(t1,L1,z) => if t1 == false then GOTO L1 else z.
                    sb.append("IFFALSE ").append(arg.getValue().getLexem()).append(" L").append(lcounter).append(": :z \n");

                    writer.write(cod.getCode());
                    writer.write(sb.toString());

                    LinkedNode<Token> else_node = arg.getParent().getChildren().get(0);
                    TokenAttrs eLabel = new TokenAttrs(else_node.getValue());
                    eLabel.setCode("L" + lcounter + ": \n");
                }
            }
            //if-body was read.
            else if (arg.getParent().getValue().getName().equals("if") && arg.getParent().getChildren().indexOf(arg) == 1) {
                Token else_node = arg.getParent().getChildren().get(0).getValue();
                Token if_body = arg.getValue();
                if (if_body instanceof TokenAttrs && else_node instanceof TokenAttrs) {
                    TokenAttrs a1 = (TokenAttrs) if_body;
                    TokenAttrs a2 = (TokenAttrs) else_node;
                    String l = a2.getCode();
                    writer.write(a1.getCode());
                    writer.write(l);//label from ELS node.
                }
            }
            //while condition was read.
            else if (arg.getParent().getValue().getName().equals("while") && arg.getParent().getChildren().indexOf(arg) == 1) {
                Token t = arg.getValue();
                if(t instanceof TokenAttrs) {
                    TokenAttrs a1 = (TokenAttrs)t;
                    lcounter++;
                    String loop = "L" + lcounter + ": ";

                    writer.write(a1.getCode());
                    writer.write("IFTRUE "+a1.getLexem()+" "+loop+" :z"+" \n");

                    //begin while-loop body.
                    writer.write(loop+"\n");
                    labels.push(loop);
                }
            }
            //while body was read.
            else if(arg.getParent().getValue().getName().equals("while") && arg.getParent().getChildren().indexOf(arg) == 0){
                tree.visitFrom(VisitorMode.POST,this::generateWhileLoop,arg.getParent().getChildren().get(1));
            }
            else if(arg.getValue() instanceof TokenAttrs){
                TokenAttrs cod = (TokenAttrs)arg.getValue();
                writer.write(cod.getCode());
            }
        }catch (IOException e){
            System.out.println("Cannot write to file.");
        }
    }
    private void generateWhileLoop(Node<Token> n){
        LinkedNode<Token> arg = (LinkedNode<Token>) n;
        try{
            if (arg.getParent().getValue().getName().equals("while") && arg.getParent().getChildren().indexOf(arg) == 1) {
                Token t = arg.getValue();
                if(t instanceof TokenAttrs) {
                    TokenAttrs a1 = (TokenAttrs)t;
                    String l = labels.top();
                    labels.pop();
                    writer.write(a1.getCode());
                    writer.write("IFTRUE "+a1.getLexem()+" "+l+" :z"+" \n");
                }
            }
            else if(arg.getValue() instanceof TokenAttrs){
                TokenAttrs cod = (TokenAttrs)arg.getValue();
                writer.write(cod.getCode());
            }
        }catch (IOException e){
            System.out.println("Cannot write to file!");
        }
    }
}
