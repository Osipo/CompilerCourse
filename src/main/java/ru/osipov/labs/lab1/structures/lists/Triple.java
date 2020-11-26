package ru.osipov.labs.lab1.structures.lists;

public class Triple<T1,T2,T3> {
    private T1 v1;
    private T2 v2;
    private T3 v3;

    public Triple(T1 v1, T2 v2, T3 v3){
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public T1 getV1(){
        return v1;
    }

    public T2 getV2(){
        return v2;
    }

    public T3 getV3(){
        return v3;
    }
}
