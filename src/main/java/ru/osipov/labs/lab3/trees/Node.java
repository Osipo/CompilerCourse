package ru.osipov.labs.lab3.trees;

public class Node<T> {
    protected T value;
    protected int idx;
    public void setValue(T val) {
        this.value = val;
    }

    public T getValue() {
        return value;
    }

    public void setIdx(int i){
        this.idx = i;
    }

    public int getIdx(){return idx;}

    public Node(T val){
        this.value = val;
    }

    public Node(){
        this.value = null;
    }

    @Override
    public String toString(){
        return value.toString();
    }
}
