package ru.osipov.labs.lab4.semantics;

public class ParameterInfo extends Entry {

    private boolean isRef = false;

    public ParameterInfo(String name,String type, int mem) {
        super(name,type, EntryCategory.PARAMETER, mem);
    }

    public void setRef(boolean f){
        this.isRef = f;
    }

    public boolean isRef(){
        return isRef;
    }

    @Override
    public String toString(){
        return "name: "+name+ ", type: "+type+", memory: "+mem+", isValue: "+!isRef+", isReference: "+isRef+"\n";
    }
}
