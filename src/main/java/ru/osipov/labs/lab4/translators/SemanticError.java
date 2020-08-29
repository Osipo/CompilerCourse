package ru.osipov.labs.lab4.translators;

public class SemanticError {
    private SemanticErrorType type;
    private String msg;

    public SemanticError(String msg, SemanticErrorType type){
        this.type = type;
        this.msg = msg;
    }

    public SemanticErrorType getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public String toString(){
        return type+" "+msg+"\n";
    }
}
