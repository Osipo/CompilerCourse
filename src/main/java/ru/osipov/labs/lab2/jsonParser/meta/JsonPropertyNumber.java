package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab2.jsonParser.jsElements.JsonElement;

public class JsonPropertyNumber extends JsonRange {

    protected int exMin;
    protected int exMax;
    public JsonPropertyNumber(String name, String type, boolean required, JsonElement e) {
        super(name, type, required, e);
    }

    public JsonPropertyNumber(String name, String type, JsonElement e) {
        super(name, type, e);
    }

    public JsonPropertyNumber(String name, String type, boolean required, JsonElement el,  int min, int max) {
        super(name, type, required, el, min, max);
    }

    public void setExMin(int exMin) {
        this.exMin = exMin;
    }

    public void setExMax(int exMax) {
        this.exMax = exMax;
    }

    public int getExMin() {
        return exMin;
    }

    public int getExMax() {
        return exMax;
    }

    public String toString(){
        return "[ type = \"" + type + "\", name = \"" + name + "\", required = \"" + required + "\", value = \"" + val.toString() + "\", exMin = \"" + exMin + "\", exMax = \"" + exMax +"\" ]";
    }
}
