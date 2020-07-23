package ru.osipov.labs.lab3.trees;


import ru.osipov.labs.lab1.structures.lists.ArrStack;

import java.util.List;

public class SequentialNRVisitor<T> extends NRVisitor<T> implements Visitor<T> {


    @Override
    public void preOrder(Tree<T> tree, Action<Node<T>> act) {
        Node<T> m = tree.root();//ROOT(T)

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount() * 2);
        STACK.push(m);
        long co = 0;
        while(!STACK.isEmpty()) {
            if(!noCount)
                co++;
            m = STACK.top();
            STACK.pop();
            List<Node<T>> ch = tree.getChildren(m);
            for(Node<T> c : ch){
                STACK.push(c);
            }
            act.perform(m);
        }
        if(!noCount)
            System.out.println("Visited: "+co);
    }
}
