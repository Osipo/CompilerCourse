package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;

import java.util.Set;

public class BreakChainNode implements Action<Node<Token>> {

    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> t = (LinkedNode<Token>) arg;
        LinkedNode<Token> p = t.getParent();
        if(p != null && p.getChildren().size() == 1){
            //set token(p) = token(t)
            //remove t from p and add its children (t_children) to p.
            p.setValue(t.getValue());
            p.getChildren().remove(t);
            for(LinkedNode<Token> c : t.getChildren()){
                p.getChildren().add(c);
                c.setParent(p);
            }
            t.setParent(null);
            t.setChildren(null);
        }
        //Recursive chain (from left or right recursion)
        else if(p != null && t.getValue().getName().equals(p.getValue().getName())){

            //Check whether a recursive N is a part or IF or WHILE operator
            //N -> if ( B ) N ELS,  ELS -> else N | empty
            //N -> while ( B ) N
            String ltok = p.getChildren().get(p.getChildren().size() - 1).getValue().getName();
            if(ltok.equals("if") || ltok.equals("while"))//it is not a part of IF or WHILE operator which may produce recursive chain as their bodies are sequence of operators.
                return;
            //delete t from p and add its children (t_children) to p.
            p.getChildren().remove(t);
            for(LinkedNode<Token> c : t.getChildren()){
                p.getChildren().add(c);
                c.setParent(p);
            }
            t.setParent(null);
            t.setChildren(null);
        }
    }
}
