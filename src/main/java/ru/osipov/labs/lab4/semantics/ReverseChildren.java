package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;

import java.util.Collections;

public class ReverseChildren<T> implements Action<Node<T>> {
    @Override
    public void perform(Node<T> arg) {
        LinkedNode<T> ln = (LinkedNode<T>) arg;
        if(ln.getChildren().size() > 1){
            Collections.reverse(ln.getChildren());
        }
    }
}
