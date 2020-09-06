package ru.osipov.labs.lab2.jsonParser.meta;

public abstract class JsonRange extends JsonProperty {
    protected int min;
    protected int max;
    public JsonRange(String name, String type, boolean required) {
        super(name, type, required);
    }
    public JsonRange(String name, String type) {
        super(name, type,false);
    }

    public JsonRange(String name,String type,boolean required, int min,int max){
        super(name,type,required);
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
}
