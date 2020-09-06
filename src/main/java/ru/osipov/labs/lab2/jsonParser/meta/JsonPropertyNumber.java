package ru.osipov.labs.lab2.jsonParser.meta;

public class JsonPropertyNumber extends JsonRange {

    protected int exMin;
    protected int exMax;
    public JsonPropertyNumber(String name, String type, boolean required) {
        super(name, type, required);
    }

    public JsonPropertyNumber(String name, String type) {
        super(name, type);
    }

    public JsonPropertyNumber(String name, String type, boolean required, int min, int max) {
        super(name, type, required, min, max);
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
}
