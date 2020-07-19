package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;

import java.util.Set;

public class BreakChainNode implements Action<Node<Token>> {

    private int c = 0;

    public int getC() {
        return c;
    }

    @Override
    public void perform(Node<Token> arg) {
        c++;
        LinkedNode<Token> t = (LinkedNode<Token>) arg;
        LinkedNode<Token> p = t.getParent();
        if(p != null && p.getChildren().size() == 1){
            t.setParent(p.getParent());//up node to 1 level
            p.getParent().getChildren().add(t);
            p.getParent().getChildren().remove(p);
        }
    }
}
