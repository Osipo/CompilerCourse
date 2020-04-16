package ru.osipov.labs.lab3.trees;

public interface Visitor<T> {
    void preOrder(Tree<T> n, Action<Node<T>> act);
    void inOrder(Tree<T> t, Action<Node<T>> act);
    void postOrder(Tree<T> n, Action<Node<T>> act);
}
