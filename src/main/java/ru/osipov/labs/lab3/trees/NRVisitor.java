package ru.osipov.labs.lab3.trees;

import ru.osipov.labs.lab1.structures.lists.ArrStack;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

public class NRVisitor<T> implements Visitor<T> {

    @Override
    public void preOrder(Tree<T> tree, Action<Node<T>> act){
        Node<T> m = tree.root();//ROOT(T)

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount());

        while(true){
            if(m != null){
                act.perform(m);//LABEL(node,TREE)
                STACK.push(m);
                m = tree.leftMostChild(m);//LEFTMOST_CHILD(node,TREE)
            }
            else{
                if(STACK.isEmpty()){
                    return;
                }
                m = tree.rightSibling(STACK.top());//RIGHT_SIBLING(TOP(S),TREE) where TOP(S) is node
                STACK.pop();//POP(S)
            }
        }
    }

    @Override
    public void inOrder(Tree<T> t, Action<Node<T>> act) {
        Node<T> m = t.root();//ROOT(T)

        if(act == null){
            act = (n) -> System.out.print(t.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(t.getCount());
        ArrStack<Node<T>> STACK2 = new ArrStack<>(t.getCount());


        while(true){
            if(m != null){
                STACK.push(m);
                m = t.leftMostChild(m);//LEFTMOST_CHILD(node,TREE) while current != null current = current.leftson
            }
            else{
                if(STACK.isEmpty()){
                    return;
                }
                //Node<T> c = STACK.Top();
                //1) 1,2 S: 4. 2)4 3 5 8 S: 4. 3) 4 6 10 S: empty.
                act.perform(STACK.top());
                m = t.rightSibling(t.leftMostChild(STACK.top()));//right son of the STACK //current = top.rightson
                STACK.pop();
                if(m != null && t.rightSibling(m) != null){
                    STACK2.push(t.rightSibling(m));//PUSH THIRD CHILD...
                }

                if(STACK.isEmpty() && m == null && !(STACK2.isEmpty())){
                    //STACK.Push(STACK2.Top());
                    //STACK2.Pop();
                    m = STACK2.top();
                    STACK2.pop();
                }
            }
        }
    }

    @Override
    public void postOrder(Tree<T> tree, Action<Node<T>> act) {
        Node<T> m = tree.root();//ROOT(T)

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount());

        while(true){
            if(m != null){
                //act.perform(m);//LABEL(node,TREE)
                STACK.push(m);
                m = tree.leftMostChild(m);//LEFTMOST_CHILD(node,TREE)
            }
            else{
                if(STACK.isEmpty()){
                    return;
                }
                act.perform(STACK.top());
                m = tree.rightSibling(STACK.top());//RIGHT_SIBLING(TOP(S),TREE) where TOP(S) is node
                STACK.pop();//POP(S)
            }
        }
    }
}
