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

/**
 * Generates IE code
 * Commands description:
 * (curly braces { and } put only for indicating of parameters. They are not part of syntax!)
 * ( '|' symbol in expressions like {p1 | p2} is used to only specify 'XOR' between usage of p1 and p2)
 *
 * TYPE OF VARIABLES IN IE code:
 *  Temporary variables have prefix :t
 *  Parameters of functions and methods have prefix :p
 *  The absence of parameter of command is indicated as :z
 *  The return value of the method has name :res
 *  {var} indicates on name of variable of any type except :z type
 *  {pname} indicates on name of variable of parameter type.
 *  {rval} indicates the pure literal value
 *  {res_type} indicates the type name
 *  {ovar} indicates on name of variable of any type.
 *  {l} indicates the label name or method name
 *  {lo} indicates optional label name which can be omit with specifying :z name.
 * COMMANDS:
 * PUTFIELD {class_name} {field_name} {value}
 *       puts value to the field field_name of the class class_name
 * GOTO {l1} {l2}
 *      moves to the instruction which is labeled with {l1}
 *      and after assigning a value to variable :res
 *      returns to the instruction which is labeled with {l2}
 *      if {l2} was not specified, that means do not return the old context
 *
 * = {var1} {res_type} {var2}
 *      copy a value from variable {var1} to the variable {var2} which has specified type {res_type}
 *  ( * | + | - | / ) {var1} {res_type} {var2}
 *      arithmetic commands which is equivalent to {var2} = {var2} ( * | + | - | / ) ({res_type}){var1}
 * PUSH_P {var}
 *    puts into stack the value of variable with name {var} or raw value {value}
 *    saves a part of execution context (old value of variables)
 * POP_P {var}
 *     extract value from the top of the stack and saves it into variable {var}
 * PARAM {var1} :z {pname}
 *     saves value from {var1} to {pname}
 * IFFALSE {var}  {l1}  {lo2}
 *     if result of variable {var} is false when move to the instruction labeled with {l1}
 *     else move to the instruction labeled with {lo2}
 *     if {lo2} is equal to :z that means if-Statement without else.
 * IFTRUE {var}  {l1}  {l2}
 *      if result of variable {var} is true when move to the instuction labeled with {l1}
 *      else move to the instruction labeled with {l2}.
 *      This command specifies a While-Loop-Statement
 */
public class IntermediateCodeGenerator implements Action<Node<Token>> {
    private LinkedTree<Token> tree;
    private Grammar G;
    private long lcounter;
    private FileWriter writer;

    public IntermediateCodeGenerator(Grammar G,LinkedTree<Token> tree,long lc){
        this.G = G;
        this.tree = tree;
        this.lcounter = lc;
        this.writer = null;
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
            fw = new FileWriter(f,false);
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
                LinkedNode<Token> mb = arg.getChildren().get(0);
                tree.visitFrom(VisitorMode.POST, this::produceCode,mb);
            }
        }catch (IOException e){
            System.out.println("Cannot write to file.");
        }
    }

    //produce code for IF-ELSE and WHILE nodes.
    private void produceCode(Node<Token> n){
        LinkedNode<Token> arg = (LinkedNode<Token>) n;
        //System.out.println(arg);
        try {
            //IF condition was read.
            if (arg.getParent().getValue().getName().equals("if") && arg.getParent().getChildren().indexOf(arg) == 2) {
                Token if_cond = arg.getValue();
                if (if_cond instanceof TokenAttrs) {
                    TokenAttrs cod = (TokenAttrs) if_cond;
                    StringBuilder sb = new StringBuilder();
                    lcounter++;
                    //ifFalse(t1,L1,z) => if t1 == false then GOTO L1 else z.
                    sb.append("IFFALSE ").append(arg.getValue().getLexem()).append(" L").append(lcounter).append(": :z \n");

                    writer.write(cod.getCode());
                    writer.write(sb.toString());

                    LinkedNode<Token> else_node = arg.getParent().getChildren().get(0);
                    TokenAttrs eLabel = new TokenAttrs(else_node.getValue());
                    eLabel.setCode("L" + lcounter + ": \n");
                    else_node.setValue(eLabel);
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
                    //while loop was a part of if body.
                    //move label from while to statements after else. (label for FALSE_WHILE which skips WHILE_loop statements)
                    if(arg.getValue().getName().equals("while")){
                        writer.write(l);//write label from ELS node

                        //a2.setCode(a1.getCode());//set FALSE_label from WHILE node to ELS node.
                        String fl = a1.getCode().substring(a1.getCode().indexOf('-') + 1);
                        if(!fl.contains("\n"))
                            fl = fl + "\n";
                        a2.setCode(fl);
                    }
                    else {
                        writer.write(a1.getCode());
                        writer.write(l);//label from ELS node.
                        a2.setCode("");//clear label from ELS node after writing.
                    }
                }
            }
            //while condition was read.
            else if (arg.getParent().getValue().getName().equals("while") && arg.getParent().getChildren().indexOf(arg) == 1) {
                Token t = arg.getValue();
                if(t instanceof TokenAttrs) {
                    TokenAttrs a1 = (TokenAttrs)t;
                    lcounter++;
                    String loop = "L" + lcounter + ": ";
                    lcounter++;
                    String eloop = "L" + lcounter + ": ";

                    writer.write(a1.getCode());
                    writer.write("IFTRUE "+a1.getLexem()+" "+loop+" "+eloop+"\n");

                    //begin while-loop body.
                    writer.write(loop+"\n");
                    

                    //save loops into WHILE Node.
                    LinkedNode<Token> p = arg.getParent();
                    TokenAttrs after_while = new TokenAttrs(p.getValue());

                    //SAVE LABELS
                    
                    after_while.setCode(loop+"-"+eloop);
                    arg.getParent().setValue(after_while);
                }
            }
            //while body was read.
            else if(arg.getParent().getValue().getName().equals("while") && arg.getParent().getChildren().indexOf(arg) == 0){
                Token t = arg.getValue();
                if(t instanceof TokenAttrs){
                    TokenAttrs end_loop = (TokenAttrs)t;
                    writer.write(end_loop.getCode());//write last command of while loop. (last statement in while body)
                }
                tree.visitFrom(VisitorMode.POST,this::generateWhileLoop,arg.getParent().getChildren().get(1));
            }
            else if(arg.getValue() instanceof TokenAttrs){
                TokenAttrs cod = (TokenAttrs)arg.getValue();
                //System.out.println("Node with code: ");
                //System.out.println(cod);
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
                   
                    writer.write(a1.getCode());
                    Token t2 = arg.getParent().getValue();
                    String l2 = "";
                    String l = "";
                    if(t2 instanceof TokenAttrs){
                        TokenAttrs l2_a = (TokenAttrs)t2;
                        //EXTRACT TRUE AND FALSE LABELS.
                        int spl = l2_a.getCode().indexOf('-');
                        l = l2_a.getCode().substring(0,spl);
                        l2 = l2_a.getCode().substring(spl + 1);
                    }
                    writer.write("IFTRUE "+a1.getLexem()+" "+l+" "+l2+" \n");
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
