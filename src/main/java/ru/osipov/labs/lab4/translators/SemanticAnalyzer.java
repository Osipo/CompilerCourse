package ru.osipov.labs.lab4.translators;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.TokenAttrs;
import ru.osipov.labs.lab3.trees.*;
import ru.osipov.labs.lab4.semantics.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//PRODUCE ANNOTATED TREE FROM given (field: parsed)
public class SemanticAnalyzer implements Action<Node<Token>> {
    private Env stable;//Symbols table (for id, vars and etc.)

    private LinkedStack<EntryCategory> S;//inner scopes.
    private LinkedStack<String> labels;//labels.
    private Set<Pair<String,Integer>> types;
    private Grammar G;
    private LinkedTree<Token> parsed;

    private long counter;//counts of temp variables.
    private long lcounter;//counts of labels.

    private SInfo currentType;
    private List<ClassInfo> classes;
    private String curClassName;
    private String curMethodName;
    private ClassInfo curClass;
    private MethodInfo curMethod;
    private EntryCategory currentScope;
    private Node<Token> currentNode;

    //IS Expression (ifTrue -> try to find definition for each variable in expression)
    private boolean isExp;
    private boolean refFlag;

    private Map<String,Set<LinkedNode<Token>>> unsearched;//for random field or method decls.
    private List<SemanticError> errors;

    private TranslatorActions actionType;

    //Accept Grammar G and Abstract Syntax tree.
    public SemanticAnalyzer(Grammar G, LinkedTree<Token> tree){
        this.G = G;
        this.parsed = tree;
        counter = 0;
        lcounter = 0;
        this.isExp = false;
        this.refFlag = false;
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
        TypeNegotiation.setGen(this);
    }

    public IntermediateCodeGenerator getCodeGenerator(){
        return new IntermediateCodeGenerator(G,parsed,lcounter);
    }

    public LinkedTree<Token> getAnnotatedParsedTree(){
        return parsed;
    }

    public void printTable(){
        stable.printTable();
    }

    public void showErrors(){
        for(SemanticError e : errors){
            System.out.println(e);
        }
    }

    public boolean hasErrors(){
        return errors.size() > 0;
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
    }

    public void reset(){
        this.currentScope = EntryCategory.GLOBAL;
        this.currentType = null;
        this.curClass = null;
        this.curMethod = null;
        this.curClassName = null;
        this.curMethodName = null;
        this.currentNode = null;
        this.isExp = false;
        this.unsearched = null;
    }

    public long getCounter() {
        return counter;
    }

    public void incCounter(){
        this.counter = counter + 1;
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
        else if(t.getLexem().equals("ref")){
            refFlag = true;
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
                errors.add(new SemanticError("Error at: ("+t.getLine()+", "+t.getColumn()+") Variable "+t.getLexem()+" is not defined!",SemanticErrorType.VARIABLE_NOT_FOUND));
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
                    errors.add(new SemanticError("Error at: ("+t.getLine()+", "+t.getColumn()+") Class "+t.getLexem()+" is already defined!",SemanticErrorType.TYPE_REDEFINED));
                }
            }
            else {
                currentType = stable.get(t.getLexem());
                if (currentType == null) {//CHECK THAT IS DEFINED.
                    errors.add(new SemanticError("Error at: ("+t.getLine()+", "+t.getColumn()+") Type "+t.getLexem()+" is not found!",SemanticErrorType.TYPE_NOT_FOUND));
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
                    p.setRef(refFlag);
                    stable.addEntry(new SInfo(t.getLexem(),p));
                    //ADD INFO ABOUT NEW PARAMERETER TO CUR_METHOD.
                    currentType = null;
                    curMethod.addParameter(p);
                    n.setRecord(p);
                    refFlag = false;
                    return;
                }
                else if(currentScope != s.getEntry().getCat()){//if it is a new entity type with same name (method with same name as field or class)
                    addInfo(n);
                    return;
                }
                else
                    errors.add(new SemanticError("Error at: ("+t.getLine()+", "+t.getColumn()+") "+t.getLexem()+" is already defined!",SemanticErrorType.TYPE_REDEFINED));
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
            p.setRef(refFlag);
            stable.addEntry(new SInfo(name,p));
            //ADD INFO TO CUR_METHOD.
            curMethod.addParameter(p);
            node.setRecord(p);
            refFlag = false;
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
        if(n.getParent() != null && n.getParent().getValue().getName().equals("def")
                && n.getParent().getChildren().indexOf(n) == 1){//id of method was found.
            currentNode = n;
            curMethod = n.getRecord() == null ? null : (MethodInfo)n.getRecord();
        }
        if(n.getValue().getName().equals("=") || n.getValue().getName().equals("return")
            || n.getValue().getName().equals("CALL")){
            parsed.visitFrom(VisitorMode.POST,this::checkExpr,n);//ERROR
        }
    }

    //assing '=' always has two children.
    //return 'return' always has one child.
    //generate IE code for type conversion and expressions.
    //Check also method calls (parameters with arguments) and compute type of CALL node.
    //Also generate code for method calls and save it into CALL node.
    private void checkExpr(Node<Token> n){
        LinkedNode<Token> arg = (LinkedNode<Token>) n;
        //System.out.println(n.getValue());
        //argument list was read (ARGS)
        if(arg.getValue().getName().equals("AL")){
            LinkedNode<Token> mId = arg.getParent().getChildren().get(1);
            //method not defined.
            if(mId.getRecord() == null){
                errors.add(new SemanticError("Error at: ("+mId.getValue().getLine()+", "+mId.getValue().getColumn()+") Cannot find method with name:  "+mId.getValue().getLexem()+".",SemanticErrorType.TYPE_NOT_FOUND));
                return;
            }
            if(mId.getRecord() instanceof MethodInfo){
                StringBuilder sb = new StringBuilder();
                MethodInfo mi = (MethodInfo) mId.getRecord();
                List<ParameterInfo> params = mi.getParams();
                List<LinkedNode<Token>> args = arg.getChildren();
                if(params.size() != args.size()){
                    errors.add(new SemanticError("Error at: ("+mId.getValue().getLine()+", "+mId.getValue().getColumn()+") Wrong number of parameters of method:  "+mId.getValue().getLexem()+". Expected:  "+params.size()+" but actual:  "+args.size(),SemanticErrorType.WRONG_PARAMS_NUMBER));
                    return;
                }

                LinkedStack<String> pStack = new LinkedStack<>();
                List<String> sentNames = new ArrayList<>();//names of variables which sent to the arg list of CALL expression as Refs
                boolean errs = false;
                for(int i = 0; i < params.size(); i++){
                    ParameterInfo param = params.get(i);
                    String ptype = param.getType();
                    String pname = param.getName();
                    String atype = args.get(i).getRecord() == null ? "Null" : args.get(i).getRecord().getType();
                    String aname = args.get(i).getRecord() == null ? ":undef" : args.get(i).getRecord().getName();
                    if(!ptype.equals(atype)){
                        errors.add(new SemanticError("Error at: ("+args.get(i).getValue().getLine()+", "+args.get(i).getValue().getColumn()+") Expected type of "+(i + 1)+" parameter:  "+ptype+" but found:  "+atype,SemanticErrorType.WRONG_TYPE));
                        errs = true;
                        continue;
                    }
                    //passing by value => SAVE INTO STACK.
                    if(!param.isRef()){
                        pStack.push(aname);
                        sb.append("PUSH_P ").append(pname).append(" \n");
                    }
                    else
                        sentNames.add(aname);
                }
                if(curMethod == null){
                    errors.add(new SemanticError("Error at: ("+currentNode.getValue().getLine()+", "+currentNode.getValue().getColumn()+"). Current method body belongs to method that is redefined!",SemanticErrorType.TYPE_REDEFINED));
                    return;
                }
                if(errs){//WRONG TYPES OF PARAMETERS
                    return;
                }
                List<ParameterInfo> oldParams = curMethod.getParams();
                //FOR EACH PARAMETER IN CURRENT METHOD SAVE THEM INTO STACK.
                for(int i = 0; i < oldParams.size();i++){
                    ParameterInfo p = oldParams.get(i);
                    if(!sentNames.contains(p.getName())) {
                        pStack.push(p.getName());
                        sb.append("PUSH_P ").append(p.getName()).append(" \n");
                    }
                }
                //THEN PUT NEW VALUES TO PARAMETERS.
                for(int i = 0; i < params.size();i++){
                    sb.append("PARAM ").append(args.get(i).getValue().getLexem()).append(" :z ").append(":p").append(i).append(" \n");
                }

                //ADD TO STACK OLD PARAMETERS OF CURRENT METHOD.
                sb.append("GOTO ").append(mi.getName()).append(":").append(" \n");
                lcounter++;
                sb.append("L").append(lcounter).append(':').append(" \n");
                while(!pStack.isEmpty()){
                    sb.append("POP_P ").append(pStack.top()).append(" \n");
                    pStack.pop();
                }
                //POP FROM STACK AFTER CALLING.
                Token v = arg.getParent().getValue();
                TokenAttrs c = new TokenAttrs(v);
                c.setCode(sb.toString());
                arg.getParent().setValue(c);
            }
            return;
        }

        //CALL NODE.
        if(arg.getValue().getName().equals("CALL")){
            LinkedNode<Token> t = arg.getChildren().get(1);//method Id node
            String type = t.getRecord() == null ? "Null" : t.getRecord().getType();

            //set type of CALL node from return type of method.
            arg.setRecord(new Entry(type,type,EntryCategory.METHOD_TYPE,0));
        }

        //if it is a single right TERM_node of CALL (method CALL with one parameter).
        else if(arg.getParent().getValue().getName().equals("CALL")
            && arg.getParent().getChildren().indexOf(arg) == 0 && arg.getValue().getType() == 't'){

            LinkedNode<Token> mId = arg.getParent().getChildren().get(1);
            //method not defined.
            if(mId.getRecord() == null){
                errors.add(new SemanticError("Error at: ("+mId.getValue().getLine()+", "+mId.getValue().getColumn()+") Cannot find method with name:  "+mId.getValue().getLexem()+".",SemanticErrorType.TYPE_NOT_FOUND));
                return;
            }
            if(mId.getRecord() instanceof MethodInfo){
                StringBuilder sb = new StringBuilder();
                MethodInfo mi = (MethodInfo) mId.getRecord();
                List<ParameterInfo> params = mi.getParams();
                String argumentName = "";
                LinkedStack<String> pStack = new LinkedStack<>();
                if(params.size() != 1){
                    errors.add(new SemanticError("Error at: ("+arg.getValue().getLine()+", "+arg.getValue().getColumn()+") Wrong number of parameters of method:  "+mId.getValue().getLexem()+". Expected:  "+params.size()+" but actual:  "+1,SemanticErrorType.WRONG_PARAMS_NUMBER));
                    return;
                }
                String ptype = params.get(0).getType();
                String aname = arg.getRecord() == null ? ":undef" : arg.getRecord().getName();
                boolean isRef = params.get(0).isRef();
                String atype = arg.getRecord() == null ? "Null" : arg.getRecord().getType();
                if(!ptype.equals(atype)){
                    errors.add(new SemanticError("Error at: ("+arg.getValue().getLine()+", "+arg.getValue().getColumn()+") Expected type of "+1+" parameter:  "+ptype+" but found:  "+atype,SemanticErrorType.WRONG_TYPE));
                    return;
                }
                if(!isRef){
                    pStack.push(aname);
                }
                else{
                    argumentName = aname;
                }
                if(curMethod == null){
                    errors.add(new SemanticError("Error at: ("+currentNode.getValue().getLine()+", "+currentNode.getValue().getColumn()+"). Current method body belongs to method that is redefined!",SemanticErrorType.TYPE_REDEFINED));
                    return;
                }
                List<ParameterInfo> oldParams = curMethod.getParams();
                //FOR EACH PARAMETER IN CURRENT METHOD SAVE THEM INTO STACK.
                for(int i = 0; i < oldParams.size();i++){
                    ParameterInfo p = oldParams.get(i);
                    if(!argumentName.equals(p.getName())) {
                        pStack.push(p.getName());
                        sb.append("PUSH_P ").append(p.getName()).append(" \n");
                    }
                }
                sb.append("PARAM ").append(arg.getValue().getLexem()).append(" :z ").append(":p0 ").append('\n');
                sb.append("GOTO ").append(mi.getName()).append(":").append(" \n");
                lcounter++;
                sb.append("L").append(lcounter).append(':').append(" \n");
                while(!pStack.isEmpty()){
                    sb.append("POP_P ").append(pStack.top()).append(" \n");
                    pStack.pop();
                }
                TokenAttrs c = new TokenAttrs(arg.getParent().getValue());
                c.setCode(sb.toString());
                arg.getParent().setValue(c);
            }
            return;
        }
        //Else if it is a single right NON_Term_node of CALL (CALL of method without parameters)
        else if(arg.getParent().getValue().getName().equals("CALL") && arg.getParent().getChildren().indexOf(arg) == 0 ){
            LinkedNode<Token> mId = arg.getParent().getChildren().get(1);
            //method not defined.
            if(mId.getRecord() == null){
                errors.add(new SemanticError("Error at: ("+mId.getValue().getLine()+", "+mId.getValue().getColumn()+") Cannot find method with name:  "+mId.getValue().getLexem()+".",SemanticErrorType.TYPE_NOT_FOUND));
                return;
            }
            if(mId.getRecord() instanceof MethodInfo) {
                MethodInfo mi = (MethodInfo)mId.getRecord();
                if (curMethod == null) {
                    errors.add(new SemanticError("Error at: (" + currentNode.getValue().getLine() + ", " + currentNode.getValue().getColumn() + "). Current method body belongs to method that is redefined!", SemanticErrorType.TYPE_REDEFINED));
                    return;
                }
                List<ParameterInfo> oldParams = curMethod.getParams();
                LinkedStack<String> pStack = new LinkedStack<>();
                StringBuilder sb = new StringBuilder();
                //FOR EACH PARAMETER IN CURRENT METHOD SAVE THEM INTO STACK.
                for (int i = 0; i < oldParams.size(); i++) {
                    ParameterInfo p = oldParams.get(i);
                    pStack.push(p.getName());
                    sb.append("PUSH_P ").append(p.getName()).append(" \n");
                }
                sb.append("GOTO ").append(mi.getName()).append(":").append(" \n");
                lcounter++;
                sb.append("L").append(lcounter).append(':').append(" \n");
                while (!pStack.isEmpty()) {
                    sb.append("POP_P ").append(pStack.top()).append(" \n");
                    pStack.pop();
                }
                TokenAttrs c = new TokenAttrs(arg.getParent().getValue());
                c.setCode(sb.toString());
                arg.getParent().setValue(c);
            }
        }
        if(arg.getValue().getName().equals("return")){
            LinkedNode<Token> t = arg.getChildren().get(0);
            String rtype = currentNode.getRecord() == null ? "Undefined" : currentNode.getRecord().getType();
            if(t.getRecord() == null){
                String tp = "Null";
                errors.add(new SemanticError("Error at: ("+t.getValue().getLine()+", "+t.getValue().getColumn()+") Expected return type:  "+rtype+" but found:  "+tp,SemanticErrorType.WRONG_TYPE));
                return;
            }
            //if both type are the same.
            if(t.getRecord().getType().equals(rtype)){
                Token exp = t.getValue();
                TokenAttrs code = new TokenAttrs(n.getValue());
                //incCounter();
                code.setCode("= "+exp.getLexem()+" "+rtype+" :res \n");
                code.setLexem(":res");
                n.setValue(code);
            }
            //else if type of expression t can be extended to method return type.
            else if(TypeNegotiation.greaterThan(currentNode,t)){//return type > type of t.
                Token exp = t.getValue();
                TokenAttrs code = new TokenAttrs(n.getValue());
                code.setCode("= "+exp.getLexem()+" "+rtype+" :res \n");
                code.setLexem(":res");
                n.setValue(code);
            }
            else
                errors.add(new SemanticError("Error at: ("+t.getValue().getLine()+", "+t.getValue().getColumn()+") Expected return type:  "+rtype+" but found:  "+t.getRecord().getType(),SemanticErrorType.WRONG_TYPE));
        }
        else if(arg.getValue().getName().equals("=")){
            LinkedNode<Token> t1 = arg.getChildren().get(0);
            LinkedNode<Token> t2 = arg.getChildren().get(1);
            String pm = "Error at: ("+t1.getValue().getLine()+", "+t1.getValue().getColumn()+") ";
            if(t1.getRecord() == null || t2.getRecord() == null){
                String tp = t1.getRecord() == null ? "Null" : t1.getRecord().getType();
                String tp2 = t2.getRecord() == null ? "Null" : t2.getRecord().getType();
                String m = t2.getRecord() == null ? "Variable not defined!" : "Expected:  "+t2.getRecord().getType()+" but found:  "+tp;
                errors.add(new SemanticError(pm+"Cannot apply operator \'"+n.getValue().getName()+"\'. "+m,SemanticErrorType.WRONG_TYPE));
                return;
            }
            //if both type are the same.
            if(t1.getRecord().getType().equals(t2.getRecord().getType())){
                Token val = t2.getValue();
                Token exp = t1.getValue();
                TokenAttrs code = new TokenAttrs(n.getValue());
                //operator ASSIGN (=) is unary or binary operator (when binary it contains type of expression to be assigned)

                code.setCode("= "+exp.getLexem()+" "+t2.getRecord().getType()+" "+val.getLexem()+" \n");
                n.setValue(code);
                //n.getRecord().setType(t2.getRecord().getType());
            }
            //else if type of expression t1 can be extended to t2.
            else if(TypeNegotiation.greaterThan(t2,t1)){//t2 > t1.
                Token val = t2.getValue();
                Token exp = t1.getValue();
                TokenAttrs code = new TokenAttrs(n.getValue());
                code.setCode("= "+exp.getLexem()+" "+t2.getRecord().getType()+" "+val.getLexem()+" \n");
                n.setValue(code);
                //n.getRecord().setType(t2.getRecord().getType());
            }
            else
                errors.add(new SemanticError(pm+"Cannot apply operator \'"+n.getValue().getName()+"\'. Expected:  "+t2.getRecord().getType()+" but found:  "+t1.getRecord().getType(),SemanticErrorType.WRONG_TYPE));
        }
        //arithmetic operator or rel operator (+,-,*,/,<,>,==,<>)
        else if(G.getOperators().contains(arg.getValue().getName())){
            Token op = n.getValue();
            //System.out.println(n.getValue()+": "+n.getValue().getName());
            //else if it is a UNARY OP.
            if(op.getName().equals("um") || op.getName().equals("up")){
                LinkedNode<Token> t1 = arg.getChildren().get(0);
                String pm = "Error at: ("+arg.getValue().getLine()+", "+arg.getValue().getColumn()+") ";
                if(arg.getRecord() == null){
                    String tp = "Null";
                    errors.add(new SemanticError(pm+"Cannot apply operator \'"+op.getName()+"\' to  "+tp+". Numeric type expected!",SemanticErrorType.WRONG_TYPE));
                    return;
                }
                if(TypeNegotiation.isNumeric(t1.getRecord())){
                    incCounter();
                    Token exp1 = t1.getValue();
                    TokenAttrs code = new TokenAttrs(op);
                    code.setCode(op.getLexem()+" "+exp1.getLexem()+":"+t1.getRecord().getType()+" :z "+":t"+counter+" \n");
                    code.setLexem(":t"+counter);
                    n.setValue(code);
                }
                else
                    errors.add(new SemanticError(pm+"Cannot apply operator \'"+op.getName()+"\' to  "+t1.getRecord().getType()+". Numeric type expected!",SemanticErrorType.WRONG_TYPE));
                return;
            }

            LinkedNode<Token> t1 = arg.getChildren().get(0);
            LinkedNode<Token> t2 = arg.getChildren().get(1);
            String pm = "Error at: ("+t1.getValue().getLine()+", "+t1.getValue().getColumn()+") ";
            if(t1.getRecord() == null || t2.getRecord() == null){
                String tp = t1.getRecord() == null ? "Null" : t1.getRecord().getType();
                String tp2 = t2.getRecord() == null ? "Null" : t2.getRecord().getType();
                errors.add(new SemanticError(pm+"Cannot apply operator \'"+n.getValue().getName()+"\' to  "+tp+"  and  "+tp2,SemanticErrorType.WRONG_TYPE));
                return;
            }
            if(t1.getRecord().getType().equals(t2.getRecord().getType())){
                incCounter();
                Token exp1 = t2.getValue();
                Token exp2 = t1.getValue();
                TokenAttrs code = new TokenAttrs(op);
                code.setCode(op.getLexem()+" "+exp1.getLexem()+":"+t2.getRecord().getType()+" "+exp2.getLexem()+":"+t1.getRecord().getType()+" "+":t"+counter+" \n");
                code.setLexem(":t"+counter);
                n.setValue(code);
            }
            //else if type of t1 can be extended to t2.
            else if(TypeNegotiation.maxType(t1,t2)){
                incCounter();
                Token exp1 = t2.getValue();
                Token exp2 = t1.getValue();
                TokenAttrs code = new TokenAttrs(op);
                code.setCode(op.getLexem()+" "+exp1.getLexem()+":"+t2.getRecord().getType()+" "+exp2.getLexem()+":"+t1.getRecord().getType()+" "+":t"+counter+" \n");
                code.setLexem(":t"+counter);//now lexeme is r where r is result of quadraple (op, e1, e2, r)
                n.setValue(code);
            }
            else
                errors.add(new SemanticError(pm+"Cannot apply operator \'"+n.getValue().getName()+"\' to  "+t1.getRecord().getType()+"  and  "+t2.getRecord().getType(),SemanticErrorType.WRONG_TYPE));
        }
    }

    //Before type checking rename params in methods bodies. (for parameters transition)
    private void renameParams(Node<Token> n){
        LinkedNode<Token> arg = (LinkedNode<Token>) n;
        if(arg.getValue().getName().equals("def")){
            currentNode = null;
        }
        else if(arg.getParent() != null && arg.getParent().getValue().getName().equals("def")
            && arg.getParent().getChildren().indexOf(arg) == 1){
            currentNode = arg;
        }
        else if(arg.getValue().getName().equals(G.getIdName()) && currentNode != null){
            Entry e = currentNode.getRecord();
            if(!(e instanceof MethodInfo)){
                errors.add(new SemanticError("Error at: ("+currentNode.getValue().getLine()+", "+currentNode.getValue().getColumn()+") Cannot find method with name:  "+currentNode.getValue().getLexem()+".",SemanticErrorType.TYPE_NOT_FOUND));
            }
            else{
                MethodInfo mi = (MethodInfo) e;
                List<String> params = mi.getParams().stream().map(Entry::getName).collect(Collectors.toList());
                //if parameter was found rename it for PARAM command.
                int idx;
                if((idx = params.indexOf(arg.getValue().getLexem())) != -1){
                    arg.getValue().setLexem(":p"+idx);
                    if(arg.getRecord() != null)
                        arg.getRecord().setNewName(":p"+idx);
                }
            }
        }
    }

    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> n = (LinkedNode<Token>) arg;
        if(actionType == TranslatorActions.SEARCH_DEFINITIONS)
            search(n);
        else if(actionType == TranslatorActions.RENAME_PARAMS)
            renameParams(n);
        else if(actionType == TranslatorActions.TYPE_CHECK)
            typeCheck(n);
    }
}