package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.jsonParser.jsElements.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonDocumentDescriptor {
    private Map<String, JsonProperty> properties;

    public JsonDocumentDescriptor(){
        this.properties = new HashMap<>();
    }

    public Map<String, ru.osipov.labs.lab2.jsonParser.meta.JsonProperty> getProperties() {
        return properties;
    }

    public void describe(JsonObject ob) {
        LinkedStack<JsonElement> S = new LinkedStack<>();
        S.push(ob);
        while (!S.isEmpty()) {
            JsonElement o = S.top();
            S.pop();
            Set<Map.Entry<String,JsonElement>> props = null;
            if(o instanceof JsonObject) {
                props = ((JsonObject) o).getValue().entrySet();

                for (Map.Entry<String, JsonElement> pv : props) {
                    String p = pv.getKey();
                    JsonElement el = pv.getValue();
                    if (el instanceof JsonString) {
                        int l = ((JsonString) el).getValue().length();
                        JsonPropertyString str = new JsonPropertyString(el, p, "string", false, l);
                        properties.put(p, str);
                    } else if (el instanceof JsonNumber) {
                        JsonPropertyNumber nump = new JsonPropertyNumber(p, "number", false, el);
                        properties.put(p, nump);
                    } else if (el instanceof JsonBoolean) {
                        JsonProperty bp = new JsonProperty(p, "boolean", false, el);
                        properties.put(p, bp);
                    } else if (el instanceof JsonArray) {
                        S.push(el);
                    } else if (el instanceof JsonNull) {
                        JsonProperty np = new JsonProperty(p, "null", false, el);
                        properties.put(p, np);
                    } else if (el instanceof JsonObject) {
                        S.push(el);
                    }
                }
            }
            else{
                ArrayList<JsonElement> arr = ((JsonArray) o).getValue();
                int idx = 0;
                String p;
                for(JsonElement el : arr){
                    p = "$_" + idx;
                    if (el instanceof JsonString) {
                        int l = ((JsonString) el).getValue().length();
                        JsonPropertyString str = new JsonPropertyString(el, p, "string", false, l);
                        properties.put(p, str);
                    } else if (el instanceof JsonNumber) {
                        JsonPropertyNumber nump = new JsonPropertyNumber(p, "number", false, el);
                        properties.put(p, nump);
                    } else if (el instanceof JsonBoolean) {
                        JsonProperty bp = new JsonProperty(p, "boolean", false, el);
                        properties.put(p, bp);
                    } else if (el instanceof JsonArray) {
                        S.push(el);
                    } else if (el instanceof JsonNull) {
                        JsonProperty np = new JsonProperty(p, "null", false, el);
                        properties.put(p, np);
                    } else if (el instanceof JsonObject) {
                        S.push(el);
                    }
                    idx++;
                }
            }
        }
    }
}
