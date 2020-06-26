package ru.osipov.labs.lab4.semantics;

public class Env {
    private STable table;
    protected Env prev;

    public Env(Env p){
        this.table = new STable();
        this.prev = p;
    }

    public void addEntry(SInfo info){
        this.table.add(info);
    }

    public SInfo get(String s){
        for(Env e = this; e != null; e = e.prev){
            SInfo found = e.table.get(s);
            if(found != null)
                return found;
        }
        return null;
    }
}
