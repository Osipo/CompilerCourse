package ru.osipov.labs.lab3.lexers;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class TokenAttrs extends Token {

    private String code;
    public TokenAttrs(Token t){
        super(t); this.code = "";
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode(){
        return code;
    }
}
