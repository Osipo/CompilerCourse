package ru.osipov.labs.lab2.grammars;

public class GrammarSymbol {
    protected char type;
    protected String val;

    public GrammarSymbol(char type, String val){
        this.type = type;
        this.val = val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setType(char type) {
        this.type = type;
    }

    public char getType() {
        return type;
    }

    @Override
    public String toString() {
        return val;
    }

    @Override
    public boolean equals(Object o){
        try{
            if(o == null)
                return false;
            GrammarSymbol s = (GrammarSymbol) o;
            return s.getType() == type && s.getVal().equals(val);
        }
        catch (ClassCastException e){
            return false;
        }
    }
}
