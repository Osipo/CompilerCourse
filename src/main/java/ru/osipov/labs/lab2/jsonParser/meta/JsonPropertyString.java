package ru.osipov.labs.lab2.jsonParser.meta;

public class JsonPropertyString extends JsonRange {

    protected String format;
    protected String regex;
    public JsonPropertyString(String name, String type, boolean required,int max) {
        super(name, type, required);
        this.format = null;
        this.regex = null;
        this.min = 0;
        this.max = max;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }


    public String getFormat() {
        return format;
    }

}
