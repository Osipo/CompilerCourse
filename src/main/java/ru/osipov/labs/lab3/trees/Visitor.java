package ru.osipov.labs.lab3.trees;

//Visit Tree n from its ROOT.
public interface Visitor<T> {
    void preOrder(Tree<T> n, Action<Node<T>> act);
    void inOrder(Tree<T> t, Action<Node<T>> act);
    void postOrder(Tree<T> n, Action<Node<T>> act);
    void setNoCount(boolean f);
    boolean isNoCount();
}