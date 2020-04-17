package ru.osipov.labs.lab1.structures.observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ObservableHashSet<T> extends HashSet<T>  {

    private List<ObservableHashSet<T>> observers;

    public ObservableHashSet(){
        this.observers = new ArrayList<>();
    }

    public void attach(ObservableHashSet<T> o) {
        observers.add(o);
        o.addAll(this);
    }

    public void detach(ObservableHashSet<T> o) {
        observers.remove(o);
    }

    public void sendMessageAll() {
        for(ObservableHashSet<T> o : observers){
            o.addAll(this);
        }
    }

    public void sendMessage(T ndata){
        for(ObservableHashSet<T> o : observers){
            o.add(ndata);
        }
    }

    public void addWithoutNotify(T t){
        super.add(t);
    }

    public void addAllWithoutNotify(Collection<? extends T> c){
        super.addAll(c);
    }

    @Override
    public boolean add(T t) {
        boolean flag =  super.add(t);
        if(flag)
            sendMessage(t);
        return flag;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean flag =  super.addAll(c);
        if(flag)
            sendMessageAll();
        return flag;
    }
}