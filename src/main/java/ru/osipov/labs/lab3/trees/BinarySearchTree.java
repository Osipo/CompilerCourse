package ru.osipov.labs.lab3.trees;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.util.*;

public class BinarySearchTree<T> implements Tree<T> {
    private LinkedBinaryNode<T> _r;
    private int _count;
    private SubVisitor<T> _visitor;
    private int _h;
    private Comparator<T> _comp;

    public BinarySearchTree(Comparator<T> comp){
        this._comp = comp;
        this._count = 0;
        this._h = 0;
        this._r = null;
        this._visitor = new BinaryNRVisitor<T>();//CHECK
    }


    //INSERT UNIQUE item.
    private void __Add(T item, LinkedBinaryNode<T> node){
        if(_count == 0){
            _r = new LinkedBinaryNode<T>();
            _r.setDepth(0);
            _r.setValue(item);
            _count += 1;
            return;
        }
        while(node != null){
            if(_comp.compare(item,node.getValue()) < 0){
                if(node.isLeftLeaf()){
                    node.setLeft(new LinkedBinaryNode<T>());
                    node.getLeft().setValue(item);
                    node.getLeft().setParent(node);
                    _count += 1;
                    return;
                }
                node = node.getLeft();
            }
            else if(_comp.compare(item,node.getValue()) > 0){
                if(node.isRightLeaf()){
                    node.setRight(new LinkedBinaryNode<T>());
                    node.getRight().setValue(item);
                    node.getRight().setParent(node);
                    _count += 1;
                    return;
                }
                node = node.getRight();
            }
        }
    }

    private T __DeleteMin(LinkedBinaryNode<T> node){
        LinkedBinaryNode<T> p = _r;
        if(_count == 0){
            return null;
        }
        while(node.getLeft() != null){
            p = node;
            node = node.getLeft();
        }
        T m = node.getValue();
        p.setLeft(node.getRight());
        _count -= 1;
        return m;
    }

    //DELETE
    private void __Delete(T item, LinkedBinaryNode<T> node){
        while(node != null){
            if(_comp.compare(item, node.getValue()) < 0){
                node = node.getLeft(); //DELETE(node.LeftSon)
            }
            else if(_comp.compare(item, node.getValue()) > 0){
                node = node.getRight();//DELETE(node.RightSon)
            }
            else if(node.isLeaf()){
                node = null;
                _count -=1;
                return;
            }
            else if(node.isLeftLeaf()){
                node = node.getRight();
                _count -= 1;
                return;
            }
            else if(node.isRightLeaf()){
                node = node.getLeft();
                _count -= 1;
                return;
            }
            else {
                node.setValue(__DeleteMin(node.getRight()));
                return;
            }
        }
    }

    //MEMBER
    private boolean __Member(T item,LinkedBinaryNode<T> node){
        if(_count == 0){
            return false;
        }
        while(node != null){
            if(_comp.compare(item,node.getValue()) == 0 || node.getValue().equals(item)){
                return true;
            }
            else if(_comp.compare(item,node.getValue()) < 0){
                node = node.getLeft();
            }
            else {
                node = node.getRight();
            }
        }
        return false;
    }

    private T __GetElem(T item,LinkedBinaryNode<T> node){
        if(_count == 0){
            return null;
        }
        while(node != null){
            if(_comp.compare(item,node.getValue()) == 0 || node.getValue().equals(item)){
                return node.getValue();
            }
            else if(_comp.compare(item,node.getValue()) < 0){
                node = node.getLeft();
            }
            else {
                node = node.getRight();
            }
        }
        return null;
    }


    public T get(T item){
        return __GetElem(item, _r);
    }

    public boolean contains(T item){
        return __Member(item,_r);//BEGIN WITH ROOT
    }


    public boolean add(T item){
        if(contains(item)){
            return false;
        }
        __Add(item,_r);
        return true;
    }

    public T deleteMin(){
        return __DeleteMin(_r);
    }

    public void delete(T item){
        if(__Member(item,_r)){
            __Delete(item,_r);
        }
    }

    @Override
    public int getCount(){
        return this._count;
    }

    @Override
    public void clear(){
        this._count = 0;
        this._h = 0;
        this._r = null;
    }

    @Override
    public Node<T> root(){
        return _r;
    }

    @Override
    public T value(Node<T> node){
        return node.getValue();
    }

    @Override
    public Node<T> parent(Node<T> node) {
        if(node == null)
            return null;
        LinkedBinaryNode<T> np = (LinkedBinaryNode<T>) node;
        return np.getParent();
    }

    @Override
    public Node<T> leftMostChild(Node<T> node){
        if(node == null)
            return null;
        LinkedBinaryNode<T> np = (LinkedBinaryNode<T>) node;
        return np.getLeft();
    }

    @Override
    public Node<T> rightSibling(Node<T> node) {
        if(node == null)
            return null;
        LinkedBinaryNode<T> np = (LinkedBinaryNode<T>) node;
        np = np.getParent();
        if(np == null || np.getRight() == null || np.getRight().getValue().equals(node.getValue())){
            return null;
        }
        return np.getRight();
    }

    @Override
    public void setVisitor(SubVisitor<T> visitor){
        this._visitor = visitor;
    }

    @Override
    public void visit(VisitorMode order, Action<Node<T>> act){
        switch(order){
            case PRE:
                _visitor.preOrder(this,act);
                break;
            case POST:
                _visitor.postOrder(this,act);
                break;
            case IN:
                _visitor.inOrder(this,act);
                break;
            default:
                break;
        }
    }

    @Override
    public <R extends Node<T>> void visitFrom(VisitorMode order, Action<Node<T>> act,R node){
        Node<Integer> nc = new Node<>(0);//Some actions MAY MODIFY count of TREE_NODES.
        switch(order){
            case PRE:
                _visitor.preOrder(this,act,node);
                break;
            case POST:
                _visitor.postOrder(this,act,node);
                break;
            case IN:
                _visitor.inOrder(this,act,node);
                break;
            case NONE:
                act.perform(node);//NONE => perform action on the root.
                break;
            default:
                break;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        if(_count == 0){
            sb.append("}");
            return sb.toString();
        }
        this._visitor.inOrder(this,new PrintAction<>(sb));//sorted.
        sb.append("}");
        return sb.toString();
    }


    public List<Node<T>> getChildren(Node<T> n){
        if(n == null)
            return null;
        LinkedBinaryNode<T> np = (LinkedBinaryNode<T>) n;
        List<Node<T>> l = new LinkedList<>();
        if(np.getLeft() != null)
            l.add(np.getLeft());
        if(np.getRight() != null)
            l.add(np.getRight());
        return l;
    }

    public String toDot(String fName){
        StringBuilder sb = new StringBuilder();
        HashSet<Node<T>> hs = new HashSet<Node<T>>();
        LinkedStack<Node<T>> STACK = new LinkedStack<>();
        Node<T> n;
        STACK.push(root());
        sb.append("digraph ").append(fName).append(" {");
        while(!STACK.isEmpty()){
            n = STACK.top();
            if(hs.contains(n)) {
                STACK.pop();
            } else {
                hs.add(n);
                String name = n.getValue().toString();
                sb.append(name).append(" [label=\"").append(name).append("\"];");
                List<Node<T>> children = getChildren(n);
                for(int c = children.size() - 1; c >= 0; c--){
                    STACK.push(children.get(c));
                    sb.append(name).append(" -> ").append(children.get(c).getValue()).append(";");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
}