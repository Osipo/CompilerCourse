package ru.osipov.labs.lab2.jsonParser.jsElements;

public class JsonString extends JsonElement<String> {
    private String val;
    public JsonString(String v){
        this.val = v;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String getValue() {
        return val;
    }

    @Override
    public String toString(){
        return val;
    }
}
