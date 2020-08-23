package ru.osipov.labs.lab1.structures.graphs;


public class Pair<T,R> {
    private T v1;
    private R v2;

    public Pair(){
        this(null,null);
    }

    public Pair(T v1, R v2){
        this.v1 = v1;
        this.v2 = v2;
    }

    public void setV1(T v1) {
        this.v1 = v1;
    }

    public void setV2(R v2) {
        this.v2 = v2;
    }

    public T getV1() {
        return v1;
    }

    public R getV2() {
        return v2;
    }

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;
        Pair<T,R> p = null;
        try{
            p = (Pair<T,R>)o;
        }
        catch (ClassCastException e){
            return false;
        }
        return p.getV1().equals(v1) && p.getV2().equals(v2);
    }

    @Override
    public int hashCode(){
        String b = getV1().toString() +
                getV2().toString();
        return b.hashCode();
    }
}
