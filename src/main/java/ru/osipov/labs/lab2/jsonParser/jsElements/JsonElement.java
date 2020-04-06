package ru.osipov.labs.lab2.jsonParser.jsElements;

import java.util.ArrayList;

public abstract class JsonElement<T> {
    public abstract T getValue();

    @Override
    public String toString(){
        return getValue().toString();
    }

    public static boolean isSimpleJsArray(JsonArray el){
        ArrayList<JsonElement> l = el.getElements();
        for(JsonElement element : l){
            if(element instanceof JsonArray || element instanceof JsonObject)
                return false;
        }
        return true;
    }
}
