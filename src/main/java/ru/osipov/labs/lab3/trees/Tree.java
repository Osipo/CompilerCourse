package ru.osipov.labs.lab3.trees;

public interface Tree<T> {
    Node<T> parent(Node<T> node);
    Node<T> leftMostChild(Node<T> node);
    Node<T> rightSibling(Node<T> node);
    Node<T> root();
    T value(Node<T> node);
    void setVisitor(Visitor<T> visitor);
    void visit(VisitorMode order, Action<Node<T>> act);
    int getCount();
    void clear();//FROM ICollection<T>
}
