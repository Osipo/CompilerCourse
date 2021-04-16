package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.structures.lists.Triple;
import ru.osipov.labs.lab2.jsonParser.jsElements.*;

import java.util.*;

public class JsonDocumentDescriptor {
    private Map<String, ArrayList<JsonProperty>> properties;

    public JsonDocumentDescriptor(){
        this.properties = new HashMap<>();
    }

    public Map<String, ArrayList<JsonProperty>> getProperties() {
        return properties;
    }

    public void merge(JsonDocumentDescriptor desc){
        if(desc == null)
            return;
        Set<Map.Entry<String, ArrayList<JsonProperty>>> props = desc.getProperties().entrySet();
        for(Map.Entry<String, ArrayList<JsonProperty>> en : props){
            if(this.properties.containsKey(en.getKey())){
                ArrayList<JsonProperty> rules = this.properties.get(en.getKey());
                rules.addAll(en.getValue());
            }
            else{
                this.properties.put(en.getKey(), en.getValue());
            }
        }
    }

    public void describe2(JsonObject root){
        LinkedStack<Triple<String, JsonElement, String>> S = new LinkedStack<>();//entity
        S.push(new Triple<>("", root, ""));
        while(!S.isEmpty()){

            String prop = S.top().getV1();
            JsonElement e = S.top().getV2();
            String c = S.top().getV3();
            S.pop();
            if(e instanceof JsonObject){
                ArrayList<JsonProperty> rules = new ArrayList<>();
                JsonProperty self_obj = new JsonProperty(prop, "object",true, e);
                rules.add(self_obj);
                properties.put(prop, rules);
                //S.push(new Triple<>(prop, new JsonLiteral("}"), c));

                //ADD <key, value> pair to the STACK.
                JsonObject ob = (JsonObject)e;
                Iterator<String> ks = ob.getValue().keySet().iterator();

                //FOR EACH PAIR create JSONPointer and add it to the Stack.
                while(ks.hasNext()) {
                    String p = ks.next();
                    p = p.replaceAll("~", "~0");//encoded by RFC 6901
                    p = p.replaceAll("/", "~1");
                    String k = prop + "/" + p;
                    S.push(new Triple<>(k, ob.getProperty(p), ""));
                }
            }
            else if(e instanceof JsonArray){
                /* Put JsonProperty for array itself*/
                ArrayList<JsonProperty> rules = new ArrayList<>();
                JsonProperty self_arr = new JsonProperty(prop, "array",true, e);
                rules.add(self_arr);
                properties.put(prop, rules);

                ArrayList<JsonElement> arr = ((JsonArray)e).getValue();
                int s = arr.size();
                int i = 0;


                JsonElement el = null;
                while(i < s){
                    el = arr.get(i);
                    String k = prop + "/" + i;
                    S.push(new Triple<>(k, el, ","));
                    i++;
                }
            }
            else if(e instanceof JsonNull){
                ArrayList<JsonProperty> rules = new ArrayList<>();
                JsonProperty np = new JsonProperty(prop, "null", false, e);
                rules.add(np);
                properties.put(prop, rules);
            }
            else if(e instanceof JsonBoolean){
                ArrayList<JsonProperty> rules = new ArrayList<>();
                JsonProperty np = new JsonProperty(prop, "boolean", false, e);
                rules.add(np);
                properties.put(prop, rules);
            }
            else if(e instanceof JsonNumber){
                ArrayList<JsonProperty> rules = new ArrayList<>();
                JsonPropertyNumber np = new JsonPropertyNumber(prop, "number", false, e);
                rules.add(np);
                properties.put(prop, rules);
            }
            else if(e instanceof JsonLiteral){
                continue;
            }
            else if(e instanceof JsonString){
                ArrayList<JsonProperty> rules = new ArrayList<>();
                JsonPropertyString np = new JsonPropertyString(prop, "string", false, e, ((JsonString) e).getValue().length());
                rules.add(np);
                properties.put(prop, rules);
            }
        }
    }
}
