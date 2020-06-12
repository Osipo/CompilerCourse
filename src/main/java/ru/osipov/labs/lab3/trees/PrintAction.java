package ru.osipov.labs.lab3.trees;

public class PrintAction<T> implements Action<Node<T>> {

    private StringBuilder arg2;

    public PrintAction(StringBuilder arg2){
        this.arg2 = arg2;
    }
    @Override
    public void perform(Node<T> arg){
        arg2.append(arg.getValue().toString()).append(" ");
    }
}
