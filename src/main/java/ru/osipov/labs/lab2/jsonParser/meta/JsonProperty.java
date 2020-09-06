package ru.osipov.labs.lab2.jsonParser.meta;

public class JsonProperty {
    protected String type;
    protected String name;
    protected boolean required;

    public JsonProperty(String name,String type,boolean required){
        this.name = name;
        this.type = type;
        this.required = required;
    }
    public JsonProperty(String name,String type){
        this(name,type,false);
    }

    public void setType(String t){
        this.type = t;
    }

    public void setRequired(boolean f){
        this.required = f;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
