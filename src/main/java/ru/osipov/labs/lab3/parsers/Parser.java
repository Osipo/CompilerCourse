package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
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
    protected EntryCategory scopeCat;//current scope (class scope, field or static field scope, method scope, local variables scope).

    public Parser(Grammar G, ILexer lexer){
        this.lexer = lexer;
        this.empty = G.getEmpty();
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

    public void openScope(){
        this.stable = new Env(stable);
    }
    public void closeScope(){
        this.stable = stable.getPrev();
    }

    protected Env getSymbolTable(){
        return stable;
    }

    public void printTable(){
        stable.printTable();
    }
}
