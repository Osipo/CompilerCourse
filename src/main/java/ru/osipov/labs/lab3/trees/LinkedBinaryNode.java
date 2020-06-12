package ru.osipov.labs.lab3.trees;

public class LinkedBinaryNode<T> extends Node<T> {
    private LinkedBinaryNode<T> left;
    private LinkedBinaryNode<T> right;
    private LinkedBinaryNode<T> parent;
    private int depth;

    public void setLeft(LinkedBinaryNode<T> left) {
        this.left = left;
    }

    public LinkedBinaryNode<T> getLeft() {
        return left;
    }

    public void setRight(LinkedBinaryNode<T> right) {
        this.right = right;
    }

    public LinkedBinaryNode<T> getRight() {
        return right;
    }

    public boolean isLeaf(){
        return left == null && right == null;
    }

    public boolean isLeftLeaf(){
        return left == null;
    }

    public boolean isRightLeaf(){
        return right == null;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setParent(LinkedBinaryNode<T> parent) {
        this.parent = parent;
    }

    public LinkedBinaryNode<T> getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object b){
        if(b == null)
            return false;
        try {
            Node<T> n = (Node<T>) b;
            return n.value.equals(value);
        }
        catch (ClassCastException e){
            return false;
        }
    }
}
