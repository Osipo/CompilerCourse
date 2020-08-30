package ru.osipov.labs.lab3.trees;

import ru.osipov.labs.lab1.structures.lists.ArrStack;

public class NRSubVisitor<T> extends NRVisitor<T> implements SubVisitor<T> {
    @Override
    public void preOrder(Tree<T> tree, Action<Node<T>> act,Node<T> node){
        Node<T> m = node;

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount() * 2);
        long c  = 0;

        while(true){
            if(m != null){
                if(!noCount)
                    c++;
                act.perform(m);//LABEL(node,TREE)
                STACK.push(m);
                m = tree.leftMostChild(m);//LEFTMOST_CHILD(node,TREE)
            }
            else{
                if(STACK.isEmpty()){
                    if(!noCount)
                        System.out.println("Visited: "+c);
                    return;
                }
                m = tree.rightSibling(STACK.top());//RIGHT_SIBLING(TOP(S),TREE) where TOP(S) is node
                STACK.pop();//POP(S)
            }
        }
    }

    @Override
    public void inOrder(Tree<T> t, Action<Node<T>> act,Node<T> node) {
        Node<T> m = node;

        if(act == null){
            act = (n) -> System.out.print(t.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(t.getCount());
        ArrStack<Node<T>> STACK2 = new ArrStack<>(t.getCount());
        long c  = 0;

        while(true){
            if(m != null){
                if(!noCount)
                    c++;
                STACK.push(m);
                m = t.leftMostChild(m);//LEFTMOST_CHILD(node,TREE) while current != null current = current.leftson
            }
            else{
                if(STACK.isEmpty()){
                    if(!noCount)
                        System.out.println("Visited: "+c);
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
    public void postOrder(Tree<T> tree, Action<Node<T>> act,Node<T> node) {
        Node<T> m = node;

        if(act == null){
            act = (n) -> System.out.print(tree.value(n).toString()+" ");
        }

        ArrStack<Node<T>> STACK = new ArrStack<>(tree.getCount());
        long c  = 0;
        while(true){
            if(m != null){
                STACK.push(m);
                m = tree.leftMostChild(m);//LEFTMOST_CHILD(node,TREE)
            }
            else{
                if(STACK.isEmpty()){
                    if(!noCount)
                        System.out.println("Visited: "+c);
                    return;
                }
                act.perform(STACK.top());
                if(!noCount)
                    c++;
                m = tree.rightSibling(STACK.top());//RIGHT_SIBLING(TOP(S),TREE) where TOP(S) is node
                STACK.pop();//POP(S)
            }
        }
    }
}
