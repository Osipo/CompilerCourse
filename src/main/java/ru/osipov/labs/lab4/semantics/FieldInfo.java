package ru.osipov.labs.lab4.semantics;

public class FieldInfo extends Entry{

    public FieldInfo(String name,String type, int mem) {
        super(name,type, EntryCategory.FIELD, mem);
    }

    //constructor copy
    public FieldInfo(FieldInfo info){
        super(info.getName(),info.getType(),info.getCat(),info.getMem());
    }

    @Override
    public FieldInfo deepClone(){
        return new FieldInfo(this);
    }


    @Override
    public String toString(){
        return "type: "+type+", name: "+name+", mem: "+mem+"\n";
    }
}
