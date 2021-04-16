package ru.osipov.labs.lab2.jsonParser.meta;

import ru.osipov.labs.lab2.jsonParser.jsElements.JsonElement;

public class JsonProperty {
    protected String type;
    protected String name;
    protected boolean required;
    protected JsonElement val;

    public JsonProperty(String name, String type, boolean required, JsonElement val){
        this.name = name;
        this.type = type;
        this.required = required;
        this.val = val;
    }
    public JsonProperty(String name, String type, JsonElement val){
        this(name,type,false,val);
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
    
    public JsonElement getVal(){
        return val;
    }

    public String toString(){
        return "[ type = \"" + type + "\", name = \"" + name + "\", required = \"" + required + "\",  value = \"" + val.toString() + "\" ]";
    }
}
