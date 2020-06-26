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
    private String currentCat;//Current scope: global, class, field, method, formal, local.
    private LinkedTree<Token> tree;

    @Override
    public void translate(LinkedTree<Token> tree, String fname){
        this.tree = tree;
        tree.visit(VisitorMode.PRE,this);
        super.translate(tree,fname);
        this.tree = null;
    }

    @Override
    public void perform(Node<Token> a) {
        LinkedNode<Token> arg = (LinkedNode<Token>)a;
        //Declaration statement.
        if(arg.getValue().getName().equals("T_7")){
            Token t = arg.getValue();
            TokenAttrs annotated = new TokenAttrs(t);
            StringWriter wr = new StringWriter();

            //get child of T node.
            wr.write(tree.getChildren(arg).get(0).getValue().getLexem()+" ");
            annotated.getAttrs().put("type",wr);//T.type = lexeme.
            arg.setValue(annotated);
            currentDInh = wr;
        }
        else if(arg.getValue().getName().equals("D_8")){
            Token t = arg.getValue();
            TokenAttrs annotated = new TokenAttrs(t);
            annotated.getAttrs().put("inh",currentDInh);//D_8.inh = T.type
            arg.setValue(annotated);
        }
        //Declare a variable.
        else if(arg.getValue().getName().equals("D_8'1")){
            Token t = arg.getValue();
            TokenAttrs annotated = new TokenAttrs(t);
            annotated.getAttrs().put("inh",currentDInh);//D_8'1.inh = D_8.inh
            //Add id to the table.
            Env st = env.top();
            SInfo entry = new SInfo(arg.getParent().getChildren().get(0).getValue().getLexem());
            entry.setEntry(new Entry(currentDInh.toString(),currentCat));//type and scope.
            st.addEntry(entry);
        }

        //Method declaration
        else if(arg.getValue().getName().equals("id") && arg.getParent().getValue().getName().equals("L_4")){
            String mid = arg.getValue().getLexem();
            SInfo entry = new SInfo(mid);
            entry.setEntry(new Entry(currentDInh.toString(),"method"));
            currentCat = "method";
            Env pst = env.top();
            pst.addEntry(entry);
            Env nst = new Env(pst);//goto new block.
            env.push(nst);
        }

        else if(arg.getValue().getName().equals("H_9")){
            Token t = arg.getValue();
            TokenAttrs annotated = new TokenAttrs(t);
            StringWriter wr = new StringWriter();

            annotated.getAttrs().put("inh",wr);
            arg.setValue(annotated);
            currentDInh = wr;
        }
        else if(arg.getValue().getName().equals("H_9'3")){
            Token t = arg.getValue();
            TokenAttrs annotated = new TokenAttrs(t);
            annotated.getAttrs().put("inh",currentDInh);//H_9'3.inh = H_9
            arg.setValue(annotated);
        }
        else if(arg.getValue().getName().equals("FP_10")){
            Token t = arg.getValue();
            TokenAttrs annotated = new TokenAttrs(t);
            annotated.getAttrs().put("inh",currentDInh);//FP_10.inh = H_9'3.inh
            arg.setValue(annotated);
        }
        //Formal parameter declaration
        else if(arg.getValue().getName().equals("F_29")){
            String type = arg.getChildren().get(0).getValue().getLexem();
            String id = arg.getChildren().get(1).getValue().getLexem();
            Env st = env.top();
            SInfo entry = new SInfo(id);
            entry.setEntry(new Entry(type,"formal"));
            st.addEntry(entry);
        }
    }
}
