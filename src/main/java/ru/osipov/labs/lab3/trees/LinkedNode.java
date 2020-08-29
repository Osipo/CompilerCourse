package ru.osipov.labs.lab3.trees;

import ru.osipov.labs.lab1.structures.lists.LinkedList;
import ru.osipov.labs.lab4.semantics.Entry;

import java.util.ArrayList;
import java.util.List;

public class LinkedNode<T> extends Node<T> {
    private List<LinkedNode<T>> children;
    private LinkedNode<T> parent;
    private Entry record;
    public LinkedNode(){
        children = new ArrayList<>();
        idx = -1;
        record = null;
    }

    public void setRecord(Entry record) {
        this.record = record;
    }

    public Entry getRecord() {
        return record;
    }

    public void setParent(LinkedNode<T> parent) {
        this.parent = parent;
    }

    public void setChildren(List<LinkedNode<T>> children) {
        this.children = children;
    }

    public LinkedNode<T> getParent() {
        return parent;
    }

    public List<LinkedNode<T>> getChildren() {
        return children;
    }
}
