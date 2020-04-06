package ru.osipov.labs.lab2.jsonParser.jsElements;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class StrLinkedHashMap<V> extends LinkedHashMap<String,V> {
    @Override
    public String toString(){
        Set<String> keys = keySet();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\t");
        Iterator<String> t = keySet().iterator();
        while(t.hasNext()){
            String k = t.next();
            sb.append(k+" : "+get(k).toString());
            if(t.hasNext())
                sb.append(",\n\t");
        }
        sb.append("}");
        return sb.toString();
    }
}
