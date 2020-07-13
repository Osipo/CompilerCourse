package ru.osipov.labs.lab4.semantics;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.*;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: Parse LL-tree.
public class MakeAstTree {

    private Set<String> operands;

    private Set<String> operators;

    private String empty;

    public MakeAstTree(Set<String> operands, Set<String> operators,String e){
        this.operands = operands;
        this.operators = operators;
        this.empty = e;
    }

    public void perform(PositionalTree<Token> t) {
       LinkedNode<Token> current = (LinkedNode<Token>) t.root();
       AtomicInteger ac = new AtomicInteger();
       t.visit(VisitorMode.PRE, (n) ->{
           if(n.getValue().getType() != 't')
               ac.addAndGet(1);
       });
       int counts = ac.get();
       System.out.println("nonTerms in tree: "+counts);
       while(counts > 0 && current != null) {
           System.out.println(current.getValue().toString()+" :: "+current.getChildren().size());
           //SELECT current node.
           if (current.getChildren().size() == 1) {
               LinkedNode<Token> nc = current.getChildren().get(0);
               if(nc.getValue().getName().equals(empty)){
                   current.getParent().getChildren().remove(current);
                   current = current.getParent();
                   counts--;
                   continue;
               }
               if(current.getValue().getType() != 't')
                   counts--;


               nc.setParent(current.getParent());
               current.getParent().getChildren().remove(current);
               current.getParent().getChildren().add(nc);
               //current.setParent(null);
               //select new node.
               current = current.getParent();
           } else {
               for (int i = 0; i < current.getChildren().size(); i++) {
                   LinkedNode<Token> c = current.getChildren().get(i);
                   if (c.getValue().getType() == 't' && !(operands.contains(c.getValue().getName()) || operators.contains(c.getValue().getName()))) {
                       current.getChildren().remove(c);
                       c.setParent(null);
                       break;
                       //do not select new node
                   }
                   else if(operators.contains(c.getValue().getName())){
                        c.setParent(null);
                        current.getChildren().remove(c);
                        current.setValue(c.getValue());
                        counts--;
                        //select new node.
                        current = selectLeftNonTerm(t);
                   }
                   else if(c.getValue().getType() != 't'){
                       current = c;
                       break;
                   }
               }
           }
       }
    }
    private LinkedNode<Token> selectLeftNonTerm(PositionalTree<Token> t){
        Node<Token> c = t.root();
        while(t.leftMostChild(c) != null){
            if(t.leftMostChild(c).getValue().getType() == 't')
                return (LinkedNode<Token>) c;
            c = t.leftMostChild(c);
        }
        if(c.getValue().getType() == 't')
            return null;
        return (LinkedNode<Token>) c;
    }
}
