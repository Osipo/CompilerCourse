package ru.osipov.labs.lab3.lexers;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class TokenAttrs extends Token {

    private Map<String, StringWriter> attrs;


    public TokenAttrs(Token t){
        super(t); this.attrs = new HashMap<>();
    }

    public Map<String, StringWriter> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, StringWriter> attrs) {
        this.attrs = attrs;
    }

    public void addAttr(String name, StringWriter output){
        this.attrs.put(name,output);
    }

    @Override
    public StringWriter getContent(){
        return this.attrs.get("code");
    }
}
