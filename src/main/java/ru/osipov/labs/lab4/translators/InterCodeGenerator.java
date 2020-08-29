package ru.osipov.labs.lab4.translators;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.Action;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.Node;
import ru.osipov.labs.lab4.semantics.*;

import java.util.*;
import java.util.stream.Collectors;

public class InterCodeGenerator implements Action<Node<Token>> {
    private Env stable;//Symbols table (for id, vars and etc.)

    private LinkedStack<EntryCategory> S;//inner scopes.
    private LinkedStack<String> labels;//labels.
    private Set<Pair<String,Integer>> types;
    private Grammar G;
    private long counter;//counts of temp variables.

    private SInfo currentType;
    private List<ClassInfo> classes;
    private String curClassName;
    private String curMethodName;
    private ClassInfo curClass;
    private MethodInfo curMethod;
    private EntryCategory currentScope;

    //IS Expression (ifTrue -> try to find definition for each variable in expression)
    private boolean isExp;

    private Map<String,Set<LinkedNode<Token>>> unsearched;
    private List<SemanticError> errors;

    private TranslatorActions actionType;

    public InterCodeGenerator(Grammar G){
        this.G = G;
        counter = 0;
        this.isExp = false;
        this.unsearched = new HashMap<>();
        this.actionType = TranslatorActions.SEARCH_DEFINITIONS;
        this.errors = new ArrayList<>();
        this.S = new LinkedStack<>();//symbol tables.
        this.labels = new LinkedStack<>();
        this.currentScope = EntryCategory.GLOBAL;
        this.classes = new ArrayList<>();//classes.
        this.stable = new Env(null);//root (dummy) table.
        this.types = G.getTypes();
        for(String s : G.getKeywords()){
            SInfo rec = new SInfo(s);
            if(G.getTypes() != null) {
                List<Pair<String, Integer>> typ = G.getTypes().stream().filter(x -> x.getV1().equals(s)).collect(Collectors.toList());
                if(typ.size() == 1) {
                    rec.setEntry(new Entry(s,s, EntryCategory.CLASS, typ.get(0).getV2()));
                }
                else{
                    rec.setEntry(new Entry(s,s,EntryCategory.GLOBAL,0));
                }
            }
            stable.addEntry(rec);
        }
    }

    public void printTable(){
        stable.printTable();
    }

    public void showErrors(){
        for(SemanticError e : errors){
            System.out.println(e);
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

    public void setActionType(TranslatorActions actionType){
        this.actionType = actionType;
        reset();
    }

    private void reset(){
        this.currentScope = EntryCategory.GLOBAL;
        this.currentType = null;
        this.curClass = null;
        this.curMethod = null;
        this.curClassName = null;
        this.curMethodName = null;
        this.isExp = false;
    }

    public TranslatorActions getActionType(){
        return actionType;
    }


    private void search(LinkedNode<Token> n){
        Token t = n.getValue();
        if(t.getType() == 'n'){
            //located expression.
            if(t.getName().equals("CALL") || t.getName().equals("AL")) {
                isExp = true;
                currentType = null;
            }
            else
                isExp = false;
            if(!t.getName().equals("D"))
                currentType = null;
            return;
        }
        //located expression.
        else if(t.getName().equals("=")){
            isExp = true;
            currentType = null;
            return;
        }
        if(t.getLexem().equals("class")){
            currentScope = EntryCategory.CLASS;
            return;
        }
        if(t.getName().equals(G.getScopeBegin())){
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
        }
        else if(t.getName().equals(G.getScopeEnd())){
            currentScope = S.top();
            //extract FIELD scope if we exit from METHOD.
            if(currentScope == EntryCategory.PARAMETER) {
                closeScope();
                S.pop();
                currentScope = S.top();
            }
            S.pop();
            closeScope();
        }
        else if(t.getLexem().equals("def")){
            currentType = null;
            currentScope = EntryCategory.METHOD;
        }
        //left id before '=' token (>>id = EXP)
        else if(isExp && n.getParent().getValue().getName().equals("=") && n.getParent().getChildren().indexOf(n) == n.getChildren().size() - 1){
            SInfo i = stable.get(t.getLexem());
            if(i == null){
                errors.add(new SemanticError("Variable "+t.getLexem()+" is not defined!",SemanticErrorType.VARIABLE_NOT_FOUND));
            }
            else
                n.setRecord(i.getEntry());
        }

        //method id in CALL or id after '=' token.
        else if(isExp){
            if(t.getName().equals(G.getIdName())){
                SInfo i = stable.get(t.getLexem());
                //if id not found SAVE INTO MAP (may be later found as FIELD or METHOD)
                if(i == null){
                    //if id of method not found.
                    if(n.getParent().getValue().getName().equals("CALL")){
                        Set<LinkedNode<Token>> nodes = unsearched.get("METHOD_"+t.getLexem());
                        if(nodes == null) {
                            nodes = new HashSet<>();
                            nodes.add(n);
                            unsearched.put("METHOD_"+t.getLexem(),nodes);
                            return;
                        }
                        nodes.add(n);
                    }
                    //id of variable in expression not found yet. (May be found later)
                    else{
                        Set<LinkedNode<Token>> nodes = unsearched.get("VAR_"+t.getLexem());
                        if(nodes == null) {
                            nodes = new HashSet<>();
                            nodes.add(n);
                            unsearched.put("VAR_"+t.getLexem(),nodes);
                            return;
                        }
                        nodes.add(n);
                    }
                }
                else
                    n.setRecord(i.getEntry());
            }
            else{//constant or operator.
                String cname = t.getName();
                //create constant variables and save them into constantTable.
                switch (cname){
                    case "num":{
                        String val = t.getLexem();
                        Entry e = null;
                        char ntype = val.charAt(val.length() - 1);//long = L,l or double = D,d or float = F,f
                        if(ntype == 'L' || ntype == 'l'){
                            e = new Entry(val.substring(0,val.length() - 1),"long",EntryCategory.CONSTANT,8);
                            n.setRecord(e);
                        }
                        else if(ntype == 'D' || ntype == 'd'){
                            e = new Entry(val.substring(0,val.length() - 1),"double",EntryCategory.CONSTANT,8);
                            n.setRecord(e);
                        }
                        else if(ntype == 'F' || ntype == 'f'){
                            e = new Entry(val.substring(0,val.length() - 1),"float",EntryCategory.CONSTANT,4);
                            n.setRecord(e);
                        }
                        else{
                            e = new Entry(val,"int",EntryCategory.CONSTANT,4);
                            n.setRecord(e);
                        }
                        break;
                    }
                    case "realNum":{
                        String val = t.getLexem();
                        Entry e;
                        char ntype = val.charAt(val.length() - 1);//long = L,l or double = D,d or float = F,f
                        if(ntype == 'D' || ntype == 'd'){
                            e = new Entry(val.substring(0,val.length() - 1),"double",EntryCategory.CONSTANT,8);
                            n.setRecord(e);
                        }
                        else if(ntype == 'F' || ntype == 'f'){
                            e = new Entry(val.substring(0,val.length() - 1),"float",EntryCategory.CONSTANT,4);
                            n.setRecord(e);
                        }
                        else{
                            e = new Entry(val,"double",EntryCategory.CONSTANT,8);
                            n.setRecord(e);
                        }
                        break;
                    }
                    case "true":{
                        Entry e = new Entry("true","bool",EntryCategory.CONSTANT,1);
                        n.setRecord(e);
                        break;
                    }
                    case "false":{
                        Entry e = new Entry("false","bool",EntryCategory.CONSTANT,1);
                        n.setRecord(e);
                        break;
                    }
                }
            }
        }

        //id OR type [DL -> >>T, D,  D -> D, id | id, T -> id, T -> basic_type]
        else if((t.getName().equals(G.getIdName()) || types.stream().map(Pair::getV1).anyMatch(x -> x.equals(t.getLexem()))) && currentType == null){
            if(currentScope == EntryCategory.CLASS){// class id
                currentType = stable.get(t.getLexem());//get by id.
                if(currentType == null){//IF NOT DEFINED
                    System.out.println("New class: "+t.getLexem());
                    ClassInfo clazz = new ClassInfo(t.getLexem(),0);
                    stable.addEntry(new SInfo(t.getLexem(),clazz));
                    curClassName = t.getLexem();
                    curClass = clazz;
                    classes.add(clazz);
                    //add new type.
                    types.add(new Pair<String,Integer>(t.getLexem(),0));
                    n.setRecord(clazz);
                }
                else{
                    errors.add(new SemanticError("Class "+t.getLexem()+" is already defined!",SemanticErrorType.TYPE_REDEFINED));
                }
            }
            else {
                currentType = stable.get(t.getLexem());
                if (currentType == null) {//CHECK THAT IS DEFINED.
                    errors.add(new SemanticError("Type "+t.getLexem()+" is not found!",SemanticErrorType.TYPE_NOT_FOUND));
                }
            }
        }
        //type T was read. next (T >> id ;).
        else if(t.getName().equals(G.getIdName()) && currentType != null){
            SInfo s = stable.get(t.getLexem());
            if(s != null) {//CHECK FOR REDEFINITIONS.
                //IF IT IS NOT A NEW PARAMETER AND CURRENT SCOPE IS PARAMETER LIST
                if(currentScope == EntryCategory.PARAMETER && currentScope != s.getEntry().getCat()) {
                    ParameterInfo p = new ParameterInfo(t.getLexem(),currentType.getValue(),currentType.getEntry().getMem());
                    stable.addEntry(new SInfo(t.getLexem(),p));
                    //ADD INFO ABOUT NEW PARAMERETER TO CUR_METHOD.
                    currentType = null;
                    curMethod.addParameter(p);
                    n.setRecord(p);
                    return;
                }
                else if(currentScope != s.getEntry().getCat()){//if it is a new entity type with same name (method with same name as field or class)
                    addInfo(n);
                    return;
                }
                else
                    errors.add(new SemanticError(t.getLexem()+" is already defined!",SemanticErrorType.TYPE_REDEFINED));
            }
            addInfo(n);
        }
    }

    private void addInfo(LinkedNode<Token> node){
        String name = node.getValue().getLexem();
        if(currentScope == EntryCategory.METHOD){
            MethodInfo m = new MethodInfo(name,currentType.getValue(),currentType.getEntry().getMem());
            stable.addEntry(new SInfo(name, m));
            currentScope = EntryCategory.PARAMETER;
            S.push(EntryCategory.FIELD);//save previous scope (FIELD).
            openScope();
            //ADD INFO ABOUT NEW METHOD TO CUR_CLASS
            currentType = null;
            curMethodName = name;
            curMethod = m;
            curClass.addMethod(m);
            node.setRecord(m);
            if(unsearched.containsKey("METHOD_"+name)){
                Set<LinkedNode<Token>> nodeSet = unsearched.get("METHOD_"+name);
                for(LinkedNode<Token> nod : nodeSet){
                    nod.setRecord(m);
                }
            }
        }
        else if(currentScope == EntryCategory.PARAMETER){
            ParameterInfo p = new ParameterInfo(name,currentType.getValue(),currentType.getEntry().getMem());
            stable.addEntry(new SInfo(name,p));
            //ADD INFO TO CUR_METHOD.
            curMethod.addParameter(p);
            node.setRecord(p);
            currentType = null;
        }
        //ADD INFO TO CUR_CLASS IF FIELD or ADD INFO TO METHOD IF VAR.
        else if(currentScope == EntryCategory.FIELD){
            FieldInfo f = new FieldInfo(name,currentType.getValue(),currentType.getEntry().getMem());
            stable.addEntry(new SInfo(name,f));
            curClass.addField(f);
            node.setRecord(f);
            if(unsearched.containsKey("VAR_"+name)){
                Set<LinkedNode<Token>> nodeSet = unsearched.get("VAR_"+name);
                for(LinkedNode<Token> nod : nodeSet){
                    nod.setRecord(f);
                }
            }
        }
        else if(currentScope == EntryCategory.VAR){
            Entry e = new Entry(name,currentType.getValue(),currentScope,currentType.getEntry().getMem());
            stable.addEntry(new SInfo(name,e));
            node.setRecord(e);
        }
    }

    //Check types of expressions. (traverse tree in POST_ORDER)
    private void typeCheck(LinkedNode<Token> n){

        //if parent is operator.
        if(n.getParent() != null && G.getOperators().contains(n.getValue().getName())){

        }
    }

    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> n = (LinkedNode<Token>) arg;
        if(actionType == TranslatorActions.SEARCH_DEFINITIONS)
            search(n);
        else if(actionType == TranslatorActions.TYPE_CHECK)
            typeCheck(n);
    }
}
