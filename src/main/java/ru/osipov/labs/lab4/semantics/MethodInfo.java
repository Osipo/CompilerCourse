package ru.osipov.labs.lab4.semantics;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo extends Entry {
    private List<ParameterInfo> params;

    public MethodInfo(String name, String type, int mem) {
        super(name,type, EntryCategory.METHOD, mem);
        this.params = new ArrayList<>();
    }

    public void addParameter(ParameterInfo p){
        this.params.add(p);
    }

    public List<ParameterInfo> getParams(){
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
