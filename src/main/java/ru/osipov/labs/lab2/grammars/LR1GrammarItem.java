package ru.osipov.labs.lab2.grammars;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab3.parsers.generators.LR_0_Automaton;

import java.util.*;

//LR(1) item
public class LR1GrammarItem extends GrammarItem {

    //2nd part of LR(1) item.
    private String term;


    public LR1GrammarItem(List<GrammarSymbol> symbols, int pos, String header,String t) {
        super(symbols, pos, header);
        this.term = t;
    }

    public LR1GrammarItem(GrammarString p, String header,String t){
        super(p,header);
        this.term = t;
    }

    public String getTerm(){
        return term;
    }

    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append('[').append(h).append(" -> ");
        for(int i = 0; i < symbols.size(); i++){
            if(i == pos)
                b.append('.');
            b.append(symbols.get(i).getVal()).append(" ");
        }
        if(pos == symbols.size())
            b.append('.');
        b.append("  { ").append(term).append(" }]");
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(super.equals(o)){
            String t = ((LR1GrammarItem)o).getTerm();
            return (this.term != null && term.equals(t)) || (this.term == null && t == null);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
