package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;

import java.util.function.Predicate;

public class BreakTailNode implements Action<Node<Token>> {
    private Predicate<LinkedNode<Token>> f;
    public BreakTailNode(){
        this.f = this::isB;
    }
    public BreakTailNode(Predicate<LinkedNode<Token>> f){
        this.f = f;
    }
    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> t = (LinkedNode<Token>) arg;
        if(f != null && f.test(t)){
            up(t);
        }
        else if(f == null && t.getValue().getName().contains("\'") && isB(t)){
            up(t);
        }
    }

    private void up(LinkedNode<Token> t){
        System.out.println(t.getValue());
        for(LinkedNode<Token> c : t.getChildren()){
            c.setParent(t.getParent());
            t.getParent().getChildren().add(c);
        }
        t.getParent().getChildren().remove(t);
        t.setParent(null);
        t.setChildren(null);
    }

    private boolean isB(LinkedNode<Token> arg){
        return arg.getParent() != null && arg.getChildren() != null && arg.getChildren().size() > 0 &&
                (arg.getParent().getChildren().indexOf(arg) == arg.getParent().getChildren().size() - 1
                || arg.getParent().getChildren().indexOf(arg) == 0);
    }
}
