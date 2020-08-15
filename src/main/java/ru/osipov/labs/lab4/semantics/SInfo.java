package ru.osipov.labs.lab4.semantics;

public class SInfo implements Value<String> {
    private String sName;//idName
    private Entry entry;

    public SInfo(String name){
        this.sName = name;
    }

    public void setName(String s){
        this.sName = s;
    }

    public void setEntry(Entry e){
        this.entry = e;
    }

    public Entry getEntry(){
        return entry;
    }

    @Override
    public String getValue(){return sName;}

    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(sName).append(": ");
        if(entry != null)
            b.append(entry.toString());
        return b.toString();
    }
}
