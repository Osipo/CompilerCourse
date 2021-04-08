package ru.osipov.labs.lab2.jsonParser.jsElements;

import ru.osipov.labs.lab1.structures.lists.Triple;
import ru.osipov.labs.lab2.jsonParser.JsParserState;
import ru.osipov.labs.lab1.structures.lists.KeyValuePair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonObject extends JsonElement<StrLinkedHashMap<JsonElement>> {
    private StrLinkedHashMap<JsonElement> table;

    public JsonObject(){
        this.table = new StrLinkedHashMap<>();
    }

    public void Put(String k, JsonElement id){
        this.table.put(k,id);
    }

    //inherits from LinkedHashMap.
    public JsonElement Get(String k){//get element on the root level.
        return table.get(k);
    }


    //get Element within object by property name.
    public JsonElement getProperty(String pname){
        return table.get(pname);
    }

    //get Element by path using path dot notation.
    public JsonElement getElement(String path){
        ArrayList<String> p = new ArrayList<>();
        Pattern regex = Pattern.compile("\\w([^.]*)");
        Matcher m = regex.matcher(path);
        while(m.find())
            p.add(m.group());
        StrLinkedHashMap<JsonElement> cnode = table;//ROOT.
        JsonElement n  = null;
        //System.out.println(p);
        if(p.size() == 0)
            return getProperty(path);
        for(String c : p){
            n = cnode.get(c);
            if(n == null)
                return n;
            try {//is interior node
                cnode = (StrLinkedHashMap<JsonElement>) n.getValue();//isTable.
            }catch (ClassCastException e){//that was leaf.
                return cnode.get(c);
            }
        }
        return n;
    }

    @Override
    public StrLinkedHashMap<JsonElement> getValue() {
        return table;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        LinkedStack<Triple<String, JsonElement,String>> S = new LinkedStack<>();//entity
        LinkedStack<Integer> T = new LinkedStack<>();//count of tabs (\t symbol)
        int tabs = 0;//current count of tabs
        T.push(0);
        S.push(new Triple<>("", this, ""));
        while(!S.isEmpty()){
            tabs = T.top();
            T.pop();
            _addTabs(sb,tabs);

            String prop = S.top().getV1();
            sb.append(prop);
            JsonElement e = S.top().getV2();
            String c = S.top().getV3();
            S.pop();
            if(e instanceof JsonObject){
                //WRITE BOUND OF OBJECT AND INCREMENT(tabs)
                sb.append('{').append('\n');

                T.push(tabs);
                S.push(new Triple<>("", new JsonLiteral("}"), c));

                //ADD <key, value> pair to the STACK.
                JsonObject ob = (JsonObject)e;
                Iterator<String> ks = ob.table.keySet().iterator();

                //FIRST ITERATION (put to stack entry without comma)
                if(ks.hasNext()) {
                    String p = ks.next();
                    String v1 = "\"" + p + "\"" + " : ";
                    S.push(new Triple<>(v1, ob.getProperty(p), ""));
                    T.push(tabs + 1);
                }

                //put others with comma
                while(ks.hasNext()){
                    String p = ks.next();
                    String v1 = "\"" + p + "\"" + " : ";
                    S.push(new Triple<>(v1, ob.getProperty(p), ","));
                    T.push(tabs + 1);
                }
            }
            else if(e instanceof JsonArray){
                ArrayList<JsonElement> arr = ((JsonArray)e).getValue();
                int s = arr.size();
                sb.append('[').append('\n');

                S.push(new Triple<>("", new JsonLiteral("]"), c));
                T.push(tabs);

                //FIRST ITERATION
                JsonElement el = arr.get(s - 1);
                s--;
                S.push(new Triple<>("", el, ""));
                T.push(tabs + 1);

                //put others with comma.
                while(s != 0){
                    el = arr.get(s - 1);
                    s--;
                    S.push(new Triple<>("", el, ","));
                    T.push(tabs + 1);
                }
            }
            else if(e instanceof JsonLiteral || e instanceof JsonNull || e instanceof JsonNumber || e instanceof JsonBoolean){
                sb.append(e.getValue().toString()).append(c).append('\n');
            }
            else if(e instanceof JsonString){
                sb.append('\"').append(((JsonString) e).getValue()).append('\"').append(c).append('\n');
            }
        }
        return sb.toString();
    }

    private void _addTabs(StringBuilder sb, int tabs){
        int k = 0;
        while(k < tabs){
            sb.append('\t');
            k++;
        }
    }
}