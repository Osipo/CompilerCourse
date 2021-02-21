package ru.osipov.labs.lab2.jsonParser.jsElements;

import java.util.ArrayList;

public class JsonArray extends JsonElement<ArrayList<JsonElement>> {
    private ArrayList<JsonElement> elements;
    private int c;
    private String pName;

    public JsonArray(){
        this.elements = new ArrayList<>();
        this.c = 0;
        this.pName = null;
    }

    public JsonArray(ArrayList<JsonElement> elements){
        this.elements = elements;
        this.c = elements.size();
        this.pName = null;
    }

    public void setElements(ArrayList<JsonElement> elements) {
        this.elements = elements;
    }

    public <T> void add(JsonElement<T> e){
        elements.add(e);
        c++;
    }

    public void setPropertyName(String pName) {
        this.pName = pName;
    }

    public String getPropertyName(){
        return pName;
    }

    public ArrayList<JsonElement> getElements() {
        return elements;
    }

    public int getC() {
        return c;
    }

    @Override
    public ArrayList<JsonElement> getValue() {
        return elements;
    }
}
