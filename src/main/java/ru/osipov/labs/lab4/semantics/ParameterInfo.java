package ru.osipov.labs.lab4.semantics;

public class ParameterInfo extends Entry {

    public ParameterInfo(String name,String type, int mem) {
        super(name,type, EntryCategory.PARAMETER, mem);
    }

    @Override
    public String toString(){
        return "name: "+name+ ", type: "+type+", memory: "+mem+"\n";
    }
}
