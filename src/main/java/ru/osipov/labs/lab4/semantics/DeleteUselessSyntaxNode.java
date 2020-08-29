package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;

public class DeleteUselessSyntaxNode implements Action<Node<Token>> {

    private Grammar G;
    public DeleteUselessSyntaxNode(Grammar G){
        this.G = G;
    }

    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> t = (LinkedNode<Token>) arg;
        if(t.getValue().getType() == 't'){
            String term = t.getValue().getName();
            if(G.getOperands().contains(term) || G.getOperators().contains(term) || G.getKeywords().contains(term) || G.getScopeBegin().equals(term) || G.getScopeEnd().equals(term))
                return;
            LinkedNode<Token> p = t.getParent();
            p.getChildren().remove(t);
            t.setParent(null);
            t.setValue(null);
        }
    }
}
