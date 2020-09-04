package ru.osipov.labs.lab4.semantics;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo extends Entry{
    private List<MethodInfo> methods;
    private List<FieldInfo> fields;

    public ClassInfo(String type,int mem){
        super(type,type,EntryCategory.CLASS,mem);
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    //constructor copy.
    public ClassInfo(ClassInfo info){
        super(info.getName(),info.getType(),info.getCat(),info.getMem());
        this.methods = new ArrayList<>(info.getMethods());
        this.fields = new ArrayList<>(info.getFields());
    }

    @Override
    public ClassInfo deepClone(){
        return new ClassInfo(this);
    }

    public void addField(FieldInfo f){this.fields.add(f);}

    public void addMethod(MethodInfo m){
        this.methods.add(m);
    }

    public List<MethodInfo> getMethods(){
        return methods;
    }

    public List<FieldInfo> getFields(){
        return fields;
    }

    public boolean containsMethod(String e){
        for(MethodInfo mi : methods){
            if(mi.getName().equals(e))
                return true;
        }
        return false;
    }

    public boolean containsField(String e){
        for(FieldInfo f : fields){
            if(f.getName().equals(e))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Class ").append(type).append("\n");
        sb.append("\tMethods: \n");
        for(MethodInfo m : methods){
            sb.append("\t\t").append(m.toString());
        }
        sb.append("\tFields: \n");
        for(FieldInfo f : fields){
            sb.append("\t\t").append(f.toString());
        }
        return sb.toString();
    }
}
