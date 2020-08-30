package ru.osipov.labs.lab4.semantics;

public class Entry {
    protected int mem;
    protected String type;
    protected EntryCategory cat;
    protected String name;

    public Entry(String name, String type, EntryCategory cat, int mem){
        this.mem = mem;
        this.cat = cat;
        this.type = type;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setNewName(String name){
        this.name = name;
    }

    public Entry(String name,String type, EntryCategory cat){
        this(name,type,cat,0);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCat(EntryCategory cat) {
        this.cat = cat;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public int getMem() {
        return mem;
    }

    public EntryCategory getCat() {
        return cat;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString(){
        return "{ type = "+type+", memory = "+mem+", category = "+cat.toString() + " }";
    }
}
