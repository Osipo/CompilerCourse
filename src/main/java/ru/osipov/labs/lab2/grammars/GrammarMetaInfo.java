package ru.osipov.labs.lab2.grammars;

import ru.osipov.labs.lab1.structures.graphs.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Meta information of Grammar for semantic actions
public class GrammarMetaInfo {
    private Set<String> keywords;//for some words like: if, while else, etc.
    private Set<String> operands;//for operator precedence grammar (operands, operators, aliases)
    private Set<String> operators;
    private Map<String,String> aliases;

    //Comments
    private String commentLine;
    private String mlStart;
    private String mlEnd;

    //For semantic actions. (Symbolic table)
    private String id;

    private Set<Pair<String,Integer>> types;//predefined types.

    //Scope boundaries (words which define a new scope)
    private String begin;
    private String end;

    //Each scope has a type which defines a rules for declarations (variables, types and etc.)
    //For example scopeCategories may be defined as a Set of Strings {CLASS, METHOD, BLOCK, GLOBAL}
    private Set<String> scopeCategories;

    public GrammarMetaInfo(){
        this.keywords = new HashSet<>();
        this.operands = new HashSet<>();
        this.operators = new HashSet<>();
        this.aliases = new HashMap<>();
        this.types = new HashSet<>();
        this.scopeCategories = new HashSet<>();
        this.begin = "";
        this.end = "";
    }

    public void setOperands(Set<String> operands) {
        if(operands != null)
             this.operands = operands;
    }

    public void setOperators(Set<String> operators) {
        if(operators != null)
            this.operators = operators;
    }

    public void setAliases(Map<String, String> aliases) {
        if(aliases != null)
            this.aliases = aliases;
    }

    public void setId(String id) {
        if(id != null)
            this.id = id;
    }

    public void setCommentLine(String commentLine) {
        if(commentLine != null)
            this.commentLine = commentLine;
    }

    public void setMlStart(String mlStart) {
        if(mlStart != null)
            this.mlStart = mlStart;
    }

    public void setMlEnd(String mlEnd) {
        if(mlEnd != null)
            this.mlEnd = mlEnd;
    }

    public void setBegin(String begin) {
        if(begin != null)
            this.begin = begin;
    }

    public void setEnd(String end) {
        if(end != null)
            this.end = end;
    }

    public void setKeywords(Set<String> keywords) {
        if(keywords != null)
            this.keywords = keywords;
    }

    public void setTypes(Set<Pair<String, Integer>> types) {
        if(types != null)
            this.types = types;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public Set<String> getOperands() {
        return operands;
    }

    public Set<String> getOperators() {
        return operators;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public String getId() {
        return id;
    }

    public String getCommentLine() {
        return commentLine;
    }

    public String getMlStart() {
        return mlStart;
    }

    public String getMlEnd() {
        return mlEnd;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public Set<Pair<String, Integer>> getTypes() {
        return types;
    }

    public void setScopeCategories(Set<String> scopeCategories) {
        this.scopeCategories = scopeCategories;
    }

    public Set<String> getScopeCategories() {
        return scopeCategories;
    }
}
