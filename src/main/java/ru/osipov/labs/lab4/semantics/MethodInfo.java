package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo extends Entry {
    private LinkedStack<ParameterInfo> params;

    public MethodInfo(String name, String type, int mem) {
        super(name,type, EntryCategory.METHOD, mem);
        this.params = new LinkedStack<>();
    }

    //copy constructor.
    public MethodInfo(MethodInfo info){
        super(info.getName(),info.getType(),info.getCat(),info.getMem());
        this.params = new LinkedStack<ParameterInfo>(info.getParams());
    }

    @Override
    public MethodInfo deepClone(){
        return new MethodInfo(this);
    }

    public void addParameter(ParameterInfo p){
        this.params.add(p);
    }

    public LinkedStack<ParameterInfo> getParams(){
        return params;
    }


    public String getReturnType(){
        return type;
    }

    @Override
    public String toString(){
        String b = "Method: " + name + "  returns " + type +
                " memory: " + mem +
                " parameters: \n";
        StringBuilder sb = new StringBuilder();
        sb.append(b);
        for(ParameterInfo p : params){
            sb.append("\t").append(p.toString());
        }
        return sb.toString();
    }
}
