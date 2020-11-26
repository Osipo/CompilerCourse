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

    /*
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Iterator<String> k = table.keySet().iterator();
        LinkedStack<KeyValuePair<String, JsonElement>> STACK = new LinkedStack<>();//entity
        LinkedStack<Integer> S2 = new LinkedStack<>();//indents
        LinkedStack<Character> matched_braces = new LinkedStack<>();//braces
        LinkedStack<Character> matched_brackets = new LinkedStack<>();//brackets.
        LinkedStack<Integer> AAI = new LinkedStack<>();//inner_arrays_flags.
        LinkedStack<JsParserState> states = new LinkedStack<>();//states for indents.
        states.push(JsParserState.OPENQP);

        KeyValuePair<String, JsonElement> ce = null;
        int ind = 1;
        while(k.hasNext()){
            String ik = k.next();
            JsonElement el = table.get(ik);
            STACK.push(new KeyValuePair<>(ik,el));
            S2.push(4);
        }
        sb.append("{\n");
        M1: while(!STACK.isEmpty()){
            ce = STACK.top();
            STACK.pop();
            ind = S2.top();
            S2.pop();
            StrLinkedHashMap<JsonElement> lo  = null;
            try{
                lo = (StrLinkedHashMap<JsonElement>) ce.getValue().getValue();
                Iterator<String> ni = lo.keySet().iterator();
                sb.append(getEmpty(ind)+ce.getKey()+" : {\n");
                while(ni.hasNext()){
                    String nk = ni.next();
                    JsonElement nel = lo.get(nk);
                    STACK.push(new KeyValuePair<>(nk,nel));
                    S2.push(ind + 4);
                }
                matched_braces.push('}');
            }
            catch (ClassCastException e){
                if(ce.getValue() instanceof JsonArray){//TODO: Move this part to the method toString() of the JsonArray.
                    ArrayList<JsonElement> arr = ((JsonArray) ce.getValue()).getElements();
                    int c = 0;
                    int obs = 0;
                    JsParserState cstate = states.top();
                    states.pop();
                    try {
                        if(cstate == JsParserState.OPENQP) {
                            throw new NumberFormatException();
                        }
                        c = Integer.parseInt(ce.getKey());
                        if(cstate == JsParserState.OPENBRACE) {
                            obs = 1;
                            cstate = states.top();
                        }
                    }
                    catch (NumberFormatException err) {
                        sb.append(getEmpty(ind) + ce.getKey() + " : [");
                        matched_brackets.push(']');
                    }
                    while(c < arr.size()){
                        JsonElement el = arr.get(c);
                        c++;
                        if(el instanceof JsonObject){
                            lo = (StrLinkedHashMap<JsonElement>) el.getValue();
                            Iterator<String> ni = lo.keySet().iterator();
                            if(obs > 0) {
                                sb.setCharAt(sb.length() - 1,',');
                                sb.append("\n"+getEmpty(ind) + "{\n");
                            }
                            else
                                sb.append("{\n");
                            STACK.push(new KeyValuePair<>(Integer.toString(c),ce.getValue()));
                            S2.push(ind);
                            states.push(JsParserState.OPENARR);
                            states.push(JsParserState.OPENBRACE);
                            while(ni.hasNext()){
                                String nk = ni.next();
                                JsonElement nel = lo.get(nk);
                                STACK.push(new KeyValuePair<>(nk,nel));
                                S2.push(ind + 4);
                                if(nel instanceof JsonArray)
                                    states.push(JsParserState.OPENQP);
                            }
                            matched_braces.push('}');
                            continue M1;
                        }
                        else if(el instanceof JsonArray){
                            sb.append("[");
                            AAI.push(c);
                            STACK.push(new KeyValuePair<>(Integer.toString(c),ce.getValue()));
                            STACK.push(new KeyValuePair<>("0",el));
                            states.push(JsParserState.OPENARR);
                            states.push(JsParserState.ARRELEM);
                            S2.push(ind);
                            S2.push(ind);
                            matched_brackets.push(']');
                            continue M1;
                        }
                        else
                            if(c < arr.size()){
                                sb.append(el+", ");
                            }
                            else{
                                sb.append(el);
                            }
                    }
                    if(cstate == JsParserState.OPENARR){//ce.getKey().equals(Integer.toString(c))) {
                        sb.append(getEmpty(ind)+matched_brackets.top());
                        matched_brackets.pop();
                        if(!STACK.isEmpty())
                            sb.append(",\n");
                        else
                            sb.append("\n");
                        continue;
                    }
                    sb.append(matched_brackets.top());
                    matched_brackets.pop();
                    if(cstate == JsParserState.ARRELEM) {
                        int q = AAI.top();
                        AAI.pop();
                        if(q < ((JsonArray) STACK.top().getValue()).getElements().size())
                            sb.append(", ");
                        continue;
                    }
                    if(!STACK.isEmpty())
                        sb.append(",\n");
                    else
                        sb.append("\n");
                }
                else {
                    sb.append(getEmpty(ind) + ce.getKey() + " : " + ce.getValue());
                    if (!STACK.isEmpty() && matched_braces.isEmpty())
                        sb.append(",\n");
                    else if (!matched_braces.isEmpty()) {
                        sb.append("\n" + getEmpty(ind - 4) + matched_braces.top());
                        matched_braces.pop();
                        if(!matched_brackets.isEmpty())
                            sb.append("\n");
                        else if(!STACK.isEmpty())
                            sb.append(",\n");
                        else
                            sb.append("\n");
                    }
                }
            }
        }
        sb.append("\n}");
        return sb.toString();
    }
     /*
    private String getEmpty(int ind){
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while(i < ind){
            sb.append(" ");
            i++;
        }
        return sb.toString();
    }*/
}