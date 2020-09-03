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

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;
        try{
            SemanticError e = (SemanticError)o;
            return e.getMsg().equalsIgnoreCase(this.msg) && e.getType() == this.type;
        }
        catch (ClassCastException c){
            return false;
        }
    }

    @Override
    public int hashCode(){
        String s = this.type + this.msg;
        return s.hashCode();
    }
}
