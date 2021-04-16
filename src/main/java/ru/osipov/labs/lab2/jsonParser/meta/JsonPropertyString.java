package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab2.jsonParser.jsElements.JsonElement;

public class JsonPropertyString extends JsonRange {

    protected String format;
    protected String regex;
    public JsonPropertyString(String name, String type, boolean required, JsonElement v, int max) {
        super(name, type, required, v);
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

    public String toString(){
        return "[ type = \"" + type + "\", name = \"" + name + "\" required = \"" + required + "\", value = \"" + val.toString() + "\", min = \"" + min + "\", max = \"" + max +"\",\n format = \"" + format + "\", regex = \"" + regex + "\"  ]";
    }

}
