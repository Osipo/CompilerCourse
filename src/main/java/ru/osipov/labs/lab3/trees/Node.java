package ru.osipov.labs.lab3.trees;

import ru.osipov.labs.lab4.semantics.Entry;

public class Node<T> {
    protected T value;
    protected int idx;
    protected Entry record;
    public void setValue(T val) {
        this.value = val;
    }

    public T getValue() {
        return value;
    }

    public void setRecord(Entry record) {
        this.record = record;
    }

    public Entry getRecord() {
        return record;
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
        this.record = null;
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
