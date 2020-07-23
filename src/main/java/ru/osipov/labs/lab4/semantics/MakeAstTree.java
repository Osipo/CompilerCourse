package ru.osipov.labs.lab4.semantics;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.*;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: Parse LL-tree.
public class MakeAstTree implements Action<Node<Token>> {

    private Set<String> operands;

    private Set<String> operators;

    public MakeAstTree(Set<String> operands, Set<String> operators){
        this.operands = operands;
        this.operators = operators;
    }

    @Override
    public void perform(Node<Token> arg) {
       LinkedNode<Token> current = (LinkedNode<Token>) arg;
       for (int i = 0; i < current.getChildren().size(); i++) {
           LinkedNode<Token> c = current.getChildren().get(i);
           if (c.getValue().getType() == 't' && !(operands.contains(c.getValue().getName()) || operators.contains(c.getValue().getName()))) {
               current.getChildren().remove(c);
               c.setParent(null);
               i = -1;
           }
           else if(operators.contains(c.getValue().getName())){
               c.setParent(null);
               current.getChildren().remove(c);
               current.setValue(c.getValue());
           }
       }
    }
}