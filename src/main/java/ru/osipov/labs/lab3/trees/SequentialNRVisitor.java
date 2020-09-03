package ru.osipov.labs.lab3.trees;


import ru.osipov.labs.lab1.structures.lists.ArrStack;

import java.util.HashSet;
import java.util.List;

//Works fine with MUTABLE TREES
//BUT USES STACK (So Children C1...CN of node N will be interpreted as CN...C1)
public class SequentialNRVisitor<T> extends NRSubVisitor<T> implements Visitor<T>, SubVisitor<T> {


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

    @Override
    public void preOrder(Tree<T> tree, Action<Node<T>> act, Node<T> node) {
        Node<T> m = node;

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

    @Override
    public void postOrder(Tree<T> tree, Action<Node<T>> act) {
        Node<T> m = tree.root();

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount() * 2);
        HashSet<Node<T>> visited = new HashSet<>();
        STACK.push(m);
        long co = 0;
        while(!STACK.isEmpty()) {
            if(!noCount)
                co++;
            m = STACK.top();
            List<Node<T>> ch = null;
            if(!visited.contains(m)){
                visited.add(m);
                ch = tree.getChildren(m);
            }
            if(ch != null && ch.size() > 0)
                for(Node<T> c : ch){
                    STACK.push(c);
                }
            else {
                act.perform(m);
                STACK.pop();
            }
        }
        if(!noCount)
            System.out.println("Visited: "+co);
    }

    @Override
    public void postOrder(Tree<T> tree, Action<Node<T>> act, Node<T> node) {
        Node<T> m = node;

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount() * 2);
        STACK.push(m);
        HashSet<Node<T>> visited = new HashSet<>();
        long co = 0;
        while(!STACK.isEmpty()) {
            if(!noCount)
                co++;
            m = STACK.top();
            List<Node<T>> ch = null;
            if(!visited.contains(m)){
                visited.add(m);
                ch = tree.getChildren(m);
            }
            if(ch != null && ch.size() > 0)
                for(Node<T> c : ch){
                    STACK.push(c);
                }
            else {
                act.perform(m);
                STACK.pop();
            }
        }
        if(!noCount)
            System.out.println("Visited: "+co);
    }
}
