package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.jsonParser.jsElements.*;

import java.util.Map;
import java.util.Set;

public class JsonDocumentDescriptor {
    private Map<String, JsonProperty> properties;

    public void describe(JsonObject ob) {
        LinkedStack<JsonObject> S = new LinkedStack<>();
        S.push(ob);
        while (!S.isEmpty()) {
            JsonObject o = S.top();
            S.pop();
            Set<String> props = o.getValue().keySet();
            for (String p : props) {
                JsonElement el = ob.getValue().get(p);
                if (el instanceof JsonString) {
                    int l = ((JsonString) el).getValue().length();
                    JsonPropertyString str = new JsonPropertyString(p, "string", false,l);
                    properties.put(p, str);
                } else if (el instanceof JsonNumber) {
                    JsonPropertyNumber nump = new JsonPropertyNumber(p,"number",false);
                    properties.put(p,nump);
                } else if (el instanceof JsonBoolean) {
                    JsonProperty bp = new JsonProperty(p,"boolean",false);
                    properties.put(p,bp);
                }
                else if(el instanceof JsonArray){

                }
                else if(el instanceof JsonNull){
                    JsonProperty np = new JsonProperty(p,"null",false);
                    properties.put(p,np);
                }
                else if(el instanceof JsonObject){
                    S.push((JsonObject)el);
                }
            }
        }
    }
}
