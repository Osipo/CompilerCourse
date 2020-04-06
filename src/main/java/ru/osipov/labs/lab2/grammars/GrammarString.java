package ru.osipov.labs.lab2.grammars;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GrammarString {
    private List<GrammarSymbol> symbols;

    public GrammarString(){
        this.symbols = new ArrayList<>(40);
    }

    public GrammarString(List<GrammarSymbol> symbols){
        this.symbols = symbols;
    }

    public void setSymbols(List<GrammarSymbol> symbols) {
        this.symbols = symbols;
    }

    public List<GrammarSymbol> getSymbols() {
        return symbols;
    }

    public void addSymbol(GrammarSymbol s){
        symbols.add(s);
    }

    public String getTypedStr(){
        StringBuilder b = new StringBuilder();
        for(GrammarSymbol s : symbols){
            b.append(s.getType());
        }
        return b.toString();
    }

    @Override
    public boolean equals(Object o){
        try{
            if(o == null)
                return false;
            GrammarString gs = (GrammarString) o;
            return gs.getSymbols().equals(symbols);
        }
        catch (ClassCastException e){
            return false;
        }
    }

    @Override
    public int hashCode(){
        StringBuilder b = new StringBuilder();
        for(GrammarSymbol s : symbols){
            b.append(s.getVal());
        }
        return b.toString().hashCode();
    }

    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        for(GrammarSymbol s : symbols){
            b.append(s.getVal());
        }
        return b.toString();
    }

    //not very yet.
    public String toString(Map<String,List<String>> lex_rules){
        StringBuilder b = new StringBuilder();
        for(GrammarSymbol s : symbols){
            b.append(s.getType() != 't' ? s.getVal() : lex_rules.get(s.getVal()).get(0));
        }
        return b.toString();
    }

    public static GrammarString getEmpty(){
        GrammarSymbol empty = new GrammarSymbol('t',"");
        ArrayList<GrammarSymbol> symbols = new ArrayList<>(1);
        symbols.add(empty);
        return new GrammarString(symbols);
    }
}
