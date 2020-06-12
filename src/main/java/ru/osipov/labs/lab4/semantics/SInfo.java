package ru.osipov.labs.lab4.semantics;

public class SInfo implements Value<String> {
    private String sName;

    public SInfo(String name){
        this.sName = name;
    }

    public void setName(String s){
        this.sName = s;
    }

    @Override
    public String getValue(){return sName;}
}
