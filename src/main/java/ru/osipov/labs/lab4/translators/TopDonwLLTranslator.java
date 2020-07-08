package ru.osipov.labs.lab4.translators;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.TokenAttrs;
import ru.osipov.labs.lab3.trees.*;
import ru.osipov.labs.lab4.semantics.Entry;
import ru.osipov.labs.lab4.semantics.Env;
import ru.osipov.labs.lab4.semantics.SInfo;

import java.io.StringWriter;
import java.util.Map;

public class TopDonwLLTranslator extends FileIntTranslator<LinkedTree<Token>,Token> implements Action<Node<Token>>, IntTranslator<LinkedTree<Token>,Token> {

    public TopDonwLLTranslator(Grammar G) {
        super(G);
    }

    private StringWriter currentDInh;
    private StringWriter currentFInh;
    private String currentCat;//Current scope: global, class, field, method, formal, local.
    private LinkedTree<Token> tree;
    private int cBlock = 0;
    private int mode = 0;
    private int temps = 0;


    @Override
    public void translate(LinkedTree<Token> tree, String fname){
        this.tree = tree;
        mode = 1;
        tree.visit(VisitorMode.PRE,this);
        mode = 0;
        tree.visit(VisitorMode.POST,this);
        super.translate(tree,fname);
        this.tree = null;
    }

    @Override
    public void perform(Node<Token> a) {
        LinkedNode<Token> arg = (LinkedNode<Token>)a;

        //PRE ORDER
        if(mode == 1) {
            //Declaration statement.
            if (arg.getValue().getName().equals("T_7")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                StringWriter wr = new StringWriter();

                //get child of T node.
                wr.write(tree.getChildren(arg).get(0).getValue().getLexem());
                annotated.getAttrs().put("type", wr);//T.type = lexeme.
                arg.setValue(annotated);//make node annotated.
                currentDInh = wr;
            } else if (arg.getValue().getName().equals("D_8")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                annotated.getAttrs().put("inh", currentDInh);//D_8.inh = T.type
                arg.setValue(annotated);
            }
            //Declare a variable.
            else if (arg.getValue().getName().equals("D_8'1")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                annotated.getAttrs().put("inh", currentDInh);//D_8'1.inh = D_8.inh
                //Add id to the table.
                Env st = env.top();
                SInfo entry = new SInfo(arg.getParent().getChildren().get(0).getValue().getLexem());
                entry.setEntry(new Entry(currentDInh.toString(), currentCat));//type and scope.
                st.addEntry(entry);
            }

            //Method declaration
            else if (arg.getValue().getName().equals(idName) && arg.getParent().getValue().getName().equals("L_4")) {
                String mid = arg.getValue().getLexem();
                SInfo entry = new SInfo(mid);//id of the method.
                entry.setEntry(new Entry(currentDInh.toString(), "method"));//type and scope
                currentCat = "method";
                Env pst = env.top();
                pst.addEntry(entry);
                Env nst = new Env(pst);//goto new block.
                env.push(nst);
            } else if (arg.getValue().getName().equals("H_9")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                StringWriter wr = new StringWriter();
                annotated.getAttrs().put("inh", wr);
                arg.setValue(annotated);
                currentDInh = wr;
            } else if (arg.getValue().getName().equals("H_9'3")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                annotated.getAttrs().put("inh", currentDInh);//H_9'3.inh = H_9
                arg.setValue(annotated);
            } else if (arg.getValue().getName().equals("FP_10")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                StringWriter wr = new StringWriter();
                annotated.getAttrs().put("inh", wr);//FP_10.inh = writer.
                arg.setValue(annotated);
                currentFInh = wr;
            }
            //Formal parameter declaration
            else if (arg.getValue().getName().equals("F_29")) {
                String type = arg.getChildren().get(0).getValue().getLexem();
                String id = arg.getChildren().get(1).getValue().getLexem();
                Env st = env.top();
                SInfo entry = new SInfo(id);
                entry.setEntry(new Entry(type, "formal", t_widths.getOrDefault(type, 0)));
                st.addEntry(entry);
                currentFInh.write("formal " + type + ":" + id + "; ");
            } else if (arg.getValue().getName().equals("H_9'1")) {
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                StringWriter wr = new StringWriter();
                annotated.getAttrs().put("inh", wr);//H_9'1.inh = writer
                arg.setValue(annotated);
                currentDInh = wr;
            }
            else if (arg.getValue().getName().equals("{") && !(arg.getParent().getValue().getName().equals("H_9'1"))){
                Env pre = env.top();
                Env nblock = new Env(pre);
                cBlock++;
                env.push(nblock);
            }
            //H_9'1 => OPLIST_12 + RE_13
            else if (arg.getValue().getName().equals("OPLIST_12") || arg.getValue().getName().equals("RE_13")){
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                StringWriter wr = new StringWriter();
                annotated.getAttrs().put("inh",wr);
                arg.setValue(annotated);
                currentDInh = wr;
            }
            else if(arg.getValue().getName().equals("P_25")){
                String id = arg.getChildren().get(0).getValue().getName();
                Env e = env.top();
                SInfo s = e.get(id);
                if(s != null){
                    TokenAttrs annotated = new TokenAttrs(arg.getValue());
                    StringWriter wr = new StringWriter();
                    wr.write(id);
                    annotated.getAttrs().put("addr",wr);
                    arg.setValue(annotated);
                }
            }
        }

        //POST ORDER
        else if(mode == 0){
            if(arg.getValue().getName().equals("}")){
                env.pop();
                cBlock--;
            }
            else if(arg.getValue().getName().equals("H_9'1")){
                StringWriter wr = ((TokenAttrs) arg.getValue()).getAttrs().get("inh");
                StringWriter wr1 = ((TokenAttrs) arg.getChildren().get(1).getValue()).getAttrs().get("inh");
                StringWriter wr2 = ((TokenAttrs) arg.getChildren().get(3).getValue()).getAttrs().get("inh");
                wr.write(wr1.toString());
                wr.write("\n");
                wr.write(wr2.toString());
            }
            else if(arg.getValue().getName().equals("(")){

            }
            //UE -> ( B_15 )
            else if(arg.getValue().getName().equals("UE_23") && arg.getChildren().size() == 3){
                Token t = arg.getValue();
                TokenAttrs annotated = new TokenAttrs(t);
                StringWriter wr1 = ((TokenAttrs) arg.getChildren().get(1).getValue()).getAttrs().get("addr");
                StringWriter wr2 = ((TokenAttrs) arg.getChildren().get(1).getValue()).getAttrs().get("code");
                annotated.getAttrs().put("addr",wr1);
                annotated.getAttrs().put("code",wr2);
                arg.setValue(annotated);
            }
            //UE -> id
            else if(arg.getValue().getName().equals("UE_23") && arg.getChildren().size() == 1){
                String id = arg.getChildren().get(0).getValue().getName();
                SInfo info = env.top().get(id);
                if(info != null){
                    TokenAttrs anno = new TokenAttrs(arg.getValue());
                    StringWriter wr = new StringWriter();
                    wr.write(id);
                    anno.getAttrs().put("addr",wr);
                    anno.getAttrs().put("code",new StringWriter());
                }
            }
            //UE -> - UE.
            else if(arg.getValue().getName().equals("UE_23") && arg.getChildren().size() == 2){
                TokenAttrs anno = new TokenAttrs(arg.getValue());
                TokenAttrs e1 = (TokenAttrs)arg.getChildren().get(1).getValue();
                StringWriter wr = new StringWriter();
                wr.write("t"+temps);
                temps++;
                anno.getAttrs().put("addr",wr);
                StringWriter wr2 = new StringWriter();
                wr2.write(e1.getContent().toString());//e1.code
                wr2.write(wr.toString()+" = minus "+e1.getAttrs().get("addr"));//temp = minus e1.addr
                anno.getAttrs().put("code",wr2);
            }
        }
    }
}
