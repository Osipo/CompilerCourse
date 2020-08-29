package ru.osipov.labs.lab4.semantics;

public class FieldInfo extends Entry{

    public FieldInfo(String name,String type, int mem) {
        super(name,type, EntryCategory.FIELD, mem);
    }

    @Override
    public String toString(){
        return "type: "+type+", name: "+name+", mem: "+mem+"\n";
    }
}
