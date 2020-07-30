package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;

public class BreakTailNode implements Action<Node<Token>> {
    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> t = (LinkedNode<Token>) arg;
        if(t.getValue().getName().contains("\'") && isB(t)){
            for(LinkedNode<Token> c : t.getChildren()){
                c.setParent(t.getParent());
                t.getParent().getChildren().add(c);
            }
            t.getParent().getChildren().remove(t);
            t.setParent(null);
            t.setChildren(null);
        }
    }

    private boolean isB(LinkedNode<Token> arg){
        return arg.getParent() != null &&
                (arg.getParent().getChildren().indexOf(arg) == arg.getParent().getChildren().size() - 1
                || arg.getParent().getChildren().indexOf(arg) == 0);
    }
}
