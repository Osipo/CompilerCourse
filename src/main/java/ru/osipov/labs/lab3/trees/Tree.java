package ru.osipov.labs.lab3.trees;

import java.util.List;

public interface Tree<T> {
    Node<T> parent(Node<T> node);
    List<Node<T>> getChildren(Node<T> n);
    Node<T> leftMostChild(Node<T> node);
    Node<T> rightSibling(Node<T> node);
    Node<T> root();
    T value(Node<T> node);
    void setVisitor(SubVisitor<T> visitor);
    void visit(VisitorMode order, Action<Node<T>> act);
    <R extends Node<T>> void visitFrom(VisitorMode order, Action<Node<T>> act, R subTree);
    int getCount();
    void clear();//FROM ICollection<T>
}
