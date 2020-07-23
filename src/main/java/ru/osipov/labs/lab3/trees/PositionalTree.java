package ru.osipov.labs.lab3.trees;

import java.util.List;

public interface PositionalTree<T> extends Tree<T> {
    void addTo(Node<T> n, T item);
    Node<T> rightMostChild(Node<T> n);
    PositionalTree<T> getSubTree(Node<T> n);
}
