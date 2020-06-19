package ru.osipov.labs.lab3.trees;

public class Node<T> {
    protected T value;
    protected int idx;
    protected String code;
    public void setValue(T val) {
        this.value = val;
    }

    public T getValue() {
        return value;
    }

    public void setCode(String c){
        this.code = c;
    }

    public String getCode(){
        return code;
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

    @Override
    public boolean equals(Object b){
        if(b == null)
            return false;
        try {
            Node<T> n = (Node<T>) b;
            return n.idx == idx;
        }
        catch (ClassCastException e){
            return false;
        }
    }

    @Override
    public int hashCode(){
        return new Integer(idx).hashCode();
    }
}
