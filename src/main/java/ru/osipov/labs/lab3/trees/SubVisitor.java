package ru.osipov.labs.lab3.trees;

//VISIT SUB_TREES WHERE ROOT(SUB_TREE) = node.
public interface SubVisitor<T> extends Visitor<T> {
    void preOrder(Tree<T> n, Action<Node<T>> act, Node<T> node);
    void inOrder(Tree<T> n, Action<Node<T>> act, Node<T> node);
    void postOrder(Tree<T> n, Action<Node<T>> act, Node<T> node);
}
