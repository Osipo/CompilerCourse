package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab4.semantics.Entry;
import ru.osipov.labs.lab4.semantics.EntryCategory;
import ru.osipov.labs.lab4.semantics.Env;
import ru.osipov.labs.lab4.semantics.SInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Parser {
    protected ILexer lexer;
    protected Env stable;//Symbols table (for id, vars and etc.)
    protected String empty;//empty symbol of grammar.
    protected boolean isParsed;
    protected boolean typeNotFound;
    protected boolean typeCheck;
    private LinkedStack<EntryCategory> S;//inner scopes.

    private SInfo currentType;
    private String scopeStart;
    private String scopeEnd;
    private String curClassName;
    private String curMethodName;
    private Set<Pair<String,Integer>> types;
    private String id;
    protected ParserMode mode;
    protected EntryCategory currentScope;//current scope (global scope,class scope, field or static field scope, method or static method scope, local variables scope).

    public void setParserMode(ParserMode mode){
        this.mode = mode;
    }

    public void setTypeChecking(boolean f){
        this.typeCheck = typeCheck;
    }

    public boolean isTypeCheck(){
        return typeCheck;
    }

    public Parser(Grammar G, ILexer lexer){
        this.lexer = lexer;
        this.isParsed = true;
        this.typeNotFound = false;
        this.currentType = null;
        this.currentScope = EntryCategory.GLOBAL;
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
        this.types = G.getTypes();
        this.S = new LinkedStack<>();
        this.stable = new Env(null);//root (dummy) table.
        for(String s : G.getKeywords()){
            SInfo rec = new SInfo(s);
            if(G.getTypes() != null) {
                List<Pair<String, Integer>> typ = G.getTypes().stream().filter(x -> x.getV1().equals(s)).collect(Collectors.toList());
                if(typ.size() == 1) {
                    rec.setEntry(new Entry(s, EntryCategory.CLASS, typ.get(0).getV2()));
                }
                else{
                    rec.setEntry(new Entry(s,EntryCategory.GLOBAL,0));
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
            if(currentScope == EntryCategory.CLASS){
                currentScope = EntryCategory.FIELD;
                S.push(EntryCategory.GLOBAL);
            }
            else if(currentScope == EntryCategory.PARAMETER){
                currentScope = EntryCategory.VAR;
                S.push(EntryCategory.PARAMETER);
            }
            else
                S.push(currentScope);//inner block with same scopeType.
            openScope();
            return true;
        }
        else if(t.getName().equals(scopeEnd)){
            currentScope = S.top();
            //extract FIELD scope if we exit from METHOD.
            if(currentScope == EntryCategory.PARAMETER) {
                closeScope();
                S.pop();
                currentScope = S.top();
            }
            S.pop();
            closeScope();
            return true;
        }
        else if(t.getLexem().equals("class")){
            currentScope = EntryCategory.CLASS;
            return true;
        }
        else if(t.getLexem().equals("def")){
            currentScope = EntryCategory.METHOD;
            return true;
        }
        else if( (t.getName().equals(id) ||
                types.stream().map(Pair::getV1).anyMatch(y -> y.equals(t.getName()))) && currentType == null){
            if(currentScope == EntryCategory.CLASS){
                currentType = stable.get(t.getLexem());
                if(currentType == null){//CHECK FOR REDIFINITION
                    System.out.println("New class: "+t.getLexem());
                    stable.addEntry(new SInfo(t.getLexem(),new Entry(t.getLexem(),EntryCategory.CLASS,0)));
                    curClassName = t.getLexem();
                    return true;
                }
                else{
                    isParsed = false;
                    System.out.println("Class "+t.getLexem()+" is already defined!");
                    return false;
                }
            }
            else {
                currentType = stable.get(t.getLexem());
                if (currentType == null) {//CHECK THAT IS DEFINED.
                    System.out.println("Type "+t.getLexem()+" is not found!");
                    typeNotFound = true;
                    isParsed = false;
                    return false;
                }
            }
        }
        else if(t.getName().equals(id) && currentType != null){
            SInfo s = stable.get(t.getLexem());
            if(s != null) {//CHECK FOR REDEFINITIONS.
                if(currentScope == EntryCategory.PARAMETER && currentScope != s.getEntry().getCat()) {
                    stable.addEntry(new SInfo(t.getLexem(),new Entry(currentType.getValue(),EntryCategory.PARAMETER,currentType.getEntry().getMem())));
                    //ADD INFO TO CUR_METHOD.
                    currentType = null;
                    return true;
                }
                isParsed = false;
                System.out.println(t.getLexem()+" is already defined!");
                return false;
            }

            if(currentScope == EntryCategory.METHOD){
                stable.addEntry(new SInfo(t.getLexem(), new Entry(currentType.getValue(),EntryCategory.METHOD,currentType.getEntry().getMem())));
                currentScope = EntryCategory.PARAMETER;
                S.push(EntryCategory.FIELD);//save previous scope (FIELD).
                openScope();
                //ADD INFO TO CUR_CLASS
                currentType = null;
                curMethodName = t.getLexem();
                return true;
            }
            else if(currentScope == EntryCategory.PARAMETER){
                stable.addEntry(new SInfo(t.getLexem(),new Entry(currentType.getValue(),EntryCategory.PARAMETER,currentType.getEntry().getMem())));
                //ADD INFO TO CUR_METHOD.
                currentType = null;
                return true;
            }
            stable.addEntry(new SInfo(t.getLexem(),new Entry(currentType.getValue(),currentScope,currentType.getEntry().getMem())));
            //ADD INFO TO CUR_CLASS IF FIELD.
            return true;
        }
        else if(!t.getName().equals(",")){//if pair of tokens are not (id, id) or (id, ,) -> forget about type.
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
