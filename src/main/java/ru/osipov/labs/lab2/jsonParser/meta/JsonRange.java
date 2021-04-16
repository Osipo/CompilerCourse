package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab2.jsonParser.jsElements.JsonElement;

public abstract class JsonRange extends JsonProperty {
    protected int min;
    protected int max;
    public JsonRange(String name, String type, boolean required, JsonElement el) {
        super(name, type, required, el);
    }
    public JsonRange(String name, String type, JsonElement el) {
        super(name, type,false,el);
    }

    public JsonRange(String name, String type, boolean required, JsonElement el,  int min, int max){
        super(name,type,required,el);
        this.min = min;
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }
    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String toString(){
        return "[ type = \"" + type + "\", name = \"" + name + "\", required = \"" + required + "\", value = \"" + val.toString() + "\", min = \"" + min + "\", max = \"" + max +"\" ]";
    }
}
