package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;
import ru.osipov.labs.lab3.lexers.Token;

public class RemoveEmptyNodes  implements Action<Node<Token>> {

    private String empty;
    public RemoveEmptyNodes(Grammar G){
        this.empty = G.getEmpty();
    }

    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> t = (LinkedNode<Token>)arg;
        if(t.getChildren().size() == 1 && t.getChildren().get(0).getValue().getName().equals(empty)){
            LinkedNode<Token> p = t.getParent();
            t.getChildren().get(0).setParent(null);
            t.setChildren(null);
            t.setParent(null);
            p.getChildren().remove(t);
        }
    }
}
