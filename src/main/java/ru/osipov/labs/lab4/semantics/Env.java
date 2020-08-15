package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.util.ArrayList;
import java.util.List;

public class Env {
    private STable table;
    protected Env prev;
    protected List<Env> children;
    public Env(Env p){
        this.table = new STable();
        this.prev = p;
        this.children = new ArrayList<>();
    }

    //USED ONLY IN PARSERS FOR SWITCHING SCOPES.
    public Env getPrev(){
        return prev;
    }

    //USED FOR TYPE CHECKING (after syntax parsing)
    public List<Env> getChildren(){
        return children;
    }

    public void addInnerScope(Env c){
        this.children.add(c);
    }

    public void printTable() {
        LinkedStack<Env> S = new LinkedStack<>();
        S.push(this);
        while(!S.isEmpty()){
            Env e = S.top();
            S.pop();
            e.table.printTable();
            for(Env c : e.getChildren()){
                S.push(c);
            }
        }
    }

    public void addEntry(SInfo info){
        this.table.add(info);
    }

    public SInfo get(String s){
        for(Env e = this; e != null; e = e.prev){
            SInfo found = e.table.get(s);
            if(found != null)
                return found;
        }
        return null;
    }
}
