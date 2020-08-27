package ru.osipov.labs.lab3.lexers;

import java.io.StringWriter;
import java.io.Writer;

public class Token {
    private String name;
    private String lexem;
    private char type;

    public Token(String name, String lexem,char type){
        this.type = type;
        this.name = name;
        this.lexem = lexem;
    }

    public Token(Token t){
        this.name = t.getName();
        this.lexem = t.getLexem();
        this.type = t.getType();
    }

    public void setType(char type) {
        this.type = type;
    }

    public void setLexem(String lexem) {
        this.lexem = lexem;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getType() {
        return type;
    }

    public String getLexem() {
        return lexem;
    }

    public String getName() {
        return name;
    }

    public StringWriter getContent(){
        return null;
    }

    @Override
    public String toString(){
        if(type == 't')
            return lexem != null ? lexem : name;
        else if(type == 'n')
            return name;
        else
            return name+"/"+lexem;
    }
}
