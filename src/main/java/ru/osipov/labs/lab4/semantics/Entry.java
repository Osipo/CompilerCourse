package ru.osipov.labs.lab4.semantics;

public class Entry {
    private int mem;
    private String type;
    private String cat;

    public Entry(String type, String cat, int mem){
        this.mem = mem;
        this.cat = cat;
        this.type = type;
    }

    public Entry(String type, String cat){
        this(type,cat,0);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public int getMem() {
        return mem;
    }

    public String getCat() {
        return cat;
    }

    public String getType() {
        return type;
    }
}
