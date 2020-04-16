package ru.osipov.labs.lab3.trees;

import ru.osipov.labs.lab1.structures.lists.LinkedList;

import java.util.ArrayList;
import java.util.List;

public class LinkedNode<T> extends Node<T> {
    private List<LinkedNode<T>> children;
    private LinkedNode<T> parent;
    public LinkedNode(){
        children = new ArrayList<>();
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
