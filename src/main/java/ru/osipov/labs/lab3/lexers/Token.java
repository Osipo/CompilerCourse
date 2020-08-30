package ru.osipov.labs.lab3.lexers;

public class Token {
    private String name;
    private String lexem;
    private int l;
    private int c;
    private char type;

    public Token(String name, String lexem,char type,int l, int c){
        this.type = type;
        this.name = name;
        this.lexem = lexem;
        this.l = l;
        this.c = c;
    }

    public Token(Token t){
        this.name = t.getName();
        this.lexem = t.getLexem();
        this.type = t.getType();
        this.l = t.getLine();
        this.c = t.getColumn();
    }

    public int getLine() {
        return l;
    }

    public int getColumn() {
        return c;
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
