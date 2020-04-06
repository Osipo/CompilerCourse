package ru.osipov.labs.lab1.structures.graphs;

public class Elem<T> {
    private T v1;

    public Elem(T v){
        this.v1 = v;
    }

    public void setV1(T v1) {
        this.v1 = v1;
    }

    public T getV1() {
        return v1;
    }
}
