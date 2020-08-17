package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab4.semantics.Entry;
import ru.osipov.labs.lab4.semantics.EntryCategory;
import ru.osipov.labs.lab4.semantics.Env;
import ru.osipov.labs.lab4.semantics.SInfo;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Parser {
    protected ILexer lexer;
    protected Env stable;//Symbols table (for id, vars and etc.)
    protected String empty;
    protected boolean isParsed;
    protected boolean typeNotFound;

    private SInfo currentType;
    private String scopeStart;
    private String scopeEnd;
    private String id;
    protected EntryCategory scopeCat;//current scope (class scope, field or static field scope, method scope, local variables scope).

    public Parser(Grammar G, ILexer lexer){
        this.lexer = lexer;
        this.isParsed = true;
        this.typeNotFound = false;
        this.currentType = null;
        this.empty = G.getEmpty();
        this.scopeStart = G.getScopeBegin();
        this.scopeEnd = G.getScopeEnd();
        this.id = G.getIdName();
        lexer.setKeywords(G.getKeywords());
        lexer.setOperands(G.getOperands());
        lexer.setAliases(G.getAliases());
        lexer.setCommentLine(G.getCommentLine());
        lexer.setMlCommentStart(G.getMlCommentStart());
        lexer.setMlCommentEnd(G.getMlCommentEnd());
        lexer.setIdName(G.getIdName());
        this.stable = new Env(null);//root (dummy) table.
        for(String s : G.getKeywords()){
            SInfo rec = new SInfo(s);
            if(G.getTypes() != null) {
                List<Pair<String, Integer>> typ = G.getTypes().stream().filter(x -> x.getV1().equals(s)).collect(Collectors.toList());
                if(typ.size() == 1) {
                    rec.setEntry(new Entry(s, EntryCategory.CLASS, typ.get(0).getV2()));
                }
            }
            stable.addEntry(rec);
        }
    }

    protected void openScope(){
        Env ns = new Env(stable);
        this.stable.addInnerScope(ns);
        this.stable = ns;
    }
    protected void closeScope(){
        this.stable = stable.getPrev();
    }

    protected boolean checkScope(Token t){
        if(t.getName().equals(scopeStart)){
            openScope();
            return true;
        }
        else if(t.getName().equals(scopeEnd)){
            closeScope();
            return true;
        }
        else if(t.getLexem().equals("class")){
            currentType = stable.get(t.getLexem());
            return true;
        }
        else if(t.getName().equals(id) && currentType == null){
            currentType = stable.get(t.getLexem());
            if(currentType == null){
                isParsed = false;
                typeNotFound = true;
                return false;
            }
        }
        else if(t.getName().equals(id) && currentType != null){
            SInfo s = stable.get(t.getLexem());
            if(s != null)
                return false;
            Entry se = s.getEntry();
            if(se != null)
                stable.addEntry(new SInfo(t.getLexem(),new Entry(currentType.getValue(),currentType.getEntry().getCat(),currentType.getEntry().getMem())));
            else
                stable.addEntry(new SInfo(t.getLexem(),new Entry(currentType.getValue(),EntryCategory.CLASS,0)));
            currentType = null;
            return true;
        }
        return true;
    }

    protected Env getSymbolTable(){
        return stable;
    }

    public void printTable(){
        stable.printTable();
    }
}
