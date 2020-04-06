package ru.osipov.labs.lab2.grammars;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.utils.ColUtils;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.jsElements.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.parallelSetAll;

//Context-Free-Grammar
public class Grammar {
    private Set<String> T;
    private Set<String> N;
    private Map<String,Set<GrammarString>> P;//left part of production is Non-terminal. (because it is only Context-Free-Grammar)
    private String S;
    private String E;//terminal which means empty String.


    private Set<String> N_g;//Non-terminals which generates words.
    private Set<String> N_e;//Non-terminals which generates empty words.
    private Map<String, List<String>> lex_rules; //terminals with their patterns. (Lexical rules)

    public Grammar(Set<String> T, Set<String> N, Map<String,Set<GrammarString>> P,String start, String em, Map<String,List<String>> lexs){
        this.T = T;
        this.N = N;
        this.P = P;
        this.S = start;
        this.E = em;
        this.lex_rules = lexs;
        computeN_g();
        computeN_e();
    }

    public Grammar(JsonObject jsonG){
        JsonElement S = jsonG.getElement("start");
        if(S instanceof JsonString)
            this.S = ((JsonString) S).getValue();
        else
            throw new InvalidJsonGrammarException("Expected start property with String value!",null);

        this.E = "";
        JsonElement T = jsonG.getElement("terms");
        if(T instanceof JsonObject){
            this.T = new HashSet<>();
            lex_rules = new HashMap<>();
            JsonObject o = (JsonObject) T;
            Set<String> termNames = o.getValue().keySet();
            for(String t : termNames){
                List<String> l = new LinkedList<>();
                this.T.add(t);
                JsonElement el = o.getElement(t);
                if(el instanceof JsonNull) {
                    if(!this.E.equals(""))
                        throw new InvalidJsonGrammarException("Only one empty-terminal is allowed. Found two or more",null);
                    this.E = t;
                    l.add("");
                }
                else if(el instanceof JsonString) {
                    String val = ((JsonString) el).getValue();
                    if(val.equals(this.E)){//value is empty string.
                        if(this.E.equals(""))
                            this.E = t;
                        else
                            throw new InvalidJsonGrammarException("Only one empty-terminal is allowed. Found two or more.",null);
                    }
                    l.add(((JsonString) el).getValue());
                }
                else if(el instanceof JsonArray){
                    ArrayList<JsonElement> patterns = ((JsonArray) el).getElements();
                    for(JsonElement pattern : patterns){
                        if(pattern instanceof JsonString){
                            l.add(((JsonString) pattern).getValue());
                        }
                        else
                            throw new InvalidJsonGrammarException("Value of terminal "+t+" is not a String pattern",null);
                    }
                }
                else
                    throw new InvalidJsonGrammarException("Value of terminal "+t+" is not a valid pattern",null);
                lex_rules.put(t,l);
            }
        }
        else
            throw new InvalidJsonGrammarException("Expected terms property with value of the JsonObject with names of terminals and their values (list of String patterns)!\n\t terms : {term_i : string | null | [string_i...] , ... n} ",null);

        JsonElement N = jsonG.getElement("nonTerms");
        if(N instanceof JsonArray){
            this.N = new HashSet<>();
            ArrayList<JsonElement> nterms = ((JsonArray) N).getElements();
            for(JsonElement e : nterms){
                if(e instanceof JsonString) {
                    this.N.add(((JsonString) e).getValue());
                }
                else
                    throw new InvalidJsonGrammarException("Expected String name of non-terminals.",null);
            }
        }
        else
            throw new InvalidJsonGrammarException("Expected nonTerms property with list of String names of non-terminals",null);

        JsonElement P = jsonG.getElement("productions");
        if(P instanceof JsonArray){
            this.P = new HashMap<>();
            ArrayList<JsonElement> rules = ((JsonArray) P).getElements();
            for(JsonElement e : rules){
                try {
                    JsonObject o = (JsonObject) e;
                    if(o.getValue().keySet().size() == 0)
                        continue;
                    String key = o.getValue().keySet().iterator().next();//get first property of object (name of production)
                    JsonElement rpart = o.Get(key);//get list of grammar symbols related to production.
                    Set<GrammarString> product_rules = this.P.get(key);//try get information about production to identify alternatives
                    if(product_rules == null)
                        product_rules = new HashSet<>();
                    if(rpart instanceof JsonArray){
                        GrammarString alpha = new GrammarString();
                        ArrayList<JsonElement> symbols = ((JsonArray) rpart).getElements();
                        for(JsonElement symbol : symbols){
                            if(symbol instanceof JsonString){
                                String X = ((JsonString) symbol).getValue();
                                GrammarSymbol entity;
                                if(this.T.contains(X))
                                    entity = new GrammarSymbol('t',X);
                                else if(this.N.contains(X)) {
                                    entity = new GrammarSymbol('n', X);
                                }
                                else
                                    throw new InvalidJsonGrammarException("Illegal grammar symbol!" +
                                            "\nExpected non-terminal or termnial of Grammar.",null);
                                alpha.addSymbol(entity);
                            }
                            else
                                throw new InvalidJsonGrammarException("Grammar Symbol must be String!",null);
                        }
                        product_rules.add(alpha);
                    }
                    else if(rpart instanceof JsonString){
                        GrammarString alpha = new GrammarString();
                        String X = ((JsonString) rpart).getValue();
                        GrammarSymbol entity;
                        if(this.T.contains(X))
                            entity = new GrammarSymbol('t',X);
                        else if(this.N.contains(X))
                            entity = new GrammarSymbol('n',X);
                        else
                            throw new InvalidJsonGrammarException("Illegal grammar symbol!" +
                                    "\nExpected non-terminal or termnial of Grammar.",null);
                        alpha.addSymbol(entity);
                        product_rules.add(alpha);
                    }
                    else if(rpart instanceof JsonNull){
                        GrammarSymbol s = new GrammarSymbol('t', this.E);
                        GrammarString alpha = new GrammarString();
                        alpha.addSymbol(s);
                        product_rules.add(alpha);
                    }
                    else
                        throw new InvalidJsonGrammarException("Illegal grammar string! Expected String, Null or list of Strings",null);
                    this.P.put(key,product_rules);
                }
                catch (ClassCastException err){
                    throw new InvalidJsonGrammarException("productions is not a list of objects!",err);
                }
            }
        }
        else
            throw new InvalidJsonGrammarException("productions is not a list!",null);
        computeN_g();
        computeN_e();
    }

    public Set<String> getTerminals() {
        return T;
    }

    public Set<String> getNonTerminals() {
        return N;
    }

    public Map<String,Set<GrammarString>> getProductions() {
        return P;
    }

    public String getStart() {
        return S;
    }

    public Map<String,List<String>> getLexicalRules(){
        return lex_rules;
    }

    public boolean isNonEmptyLanguage() {
        return N_g.contains("S");
    }

    public Set<String> getN_e(){
        return N_e;
    }

    public Set<String> getN_g(){return N_g;}

    //Compute set of non-terminals which generates empty-strings
    private void computeN_e(){
        Set<String> N_0 = new HashSet<>();
        Set<String> N_i = new HashSet<>();
        LinkedStack<Set<String>> S = new LinkedStack<>();
        S.push(N_0);
        boolean t1 = true;
        while(true){
            N_0 = S.top();
            Set<String> rules = P.keySet();
            for (String p : rules) {
                Set<GrammarString> a = P.get(p);//get all grammar strings with rule p.
                for (GrammarString alpha : a) {//for each alternative
                    List<GrammarSymbol> symbols = alpha.getSymbols();
                    for (GrammarSymbol s : symbols) {//check that alpha is (N_i-1  U  T)*
                        if (!s.getVal().equals(this.E) && !(N_0.contains(s.getVal()))) {
                            t1 = false;
                            break;//stop scanning string
                        }
                    }
                    if(t1) { //valid grammar string -> add rule.
                        N_i.add(p);
                        break;//stop scanning alternatives.
                    }
                    t1 = true;//continue scanning rule P for valid alternatives (grammar strings)
                }
            }
            S.pop();
            S.push(N_i);
            if(N_i.equals(N_0)) {
                N_e = N_i;
                return;
            }
            N_i = new HashSet<>();
        }
    }

    //Compute set of non-terminals which generate strings of Language L(G).
    private void computeN_g(){
        Set<String> N_0 = new HashSet<>();
        Set<String> N_i = new HashSet<>();
        LinkedStack<Set<String>> S = new LinkedStack<>();
        S.push(N_0);
        boolean t1 = true;
        while(true){
            N_0 = S.top();
            N_i.addAll(N_0);
            Set<String> rules = P.keySet();
            for (String p : rules) {
                Set<GrammarString> a = P.get(p);//get all grammar strings with rule p.
                for (GrammarString alpha : a) {//for each alternative
                    List<GrammarSymbol> symbols = alpha.getSymbols();
                    for (GrammarSymbol s : symbols) {//check that alpha is (N_i-1  U  T)*
                        if (s.getType() != 't' && !(N_0.contains(s.getVal()))) {
                            t1 = false;
                            break;//stop scanning string
                        }
                    }
                    if(t1) { //valid grammar string -> add rule.
                        N_i.add(p);
                        break;//stop scanning alternatives.
                    }
                    t1 = true;//continue scanning rule P for valid alternatives (grammar strings)
                }
            }
            S.pop();
            S.push(N_i);
            if(N_i.equals(N_0)) {
                N_g = N_i;
                return;
            }
            N_i = new HashSet<>();
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Grammar\n\tT: "+this.T);
        b.append("\n\tN: "+this.N);
        Set<String> kp = P.keySet();
        b.append("\n\tP {");

        b.append("\n\t\t"+this.S+" -> ");
        Iterator<GrammarString> S_r = P.get(this.S).iterator();
        b.append(S_r.next());
        while(S_r.hasNext()){
            b.append("|").append(S_r.next());
        }

        kp = P.keySet().stream().filter(x -> !x.equals(getStart())).collect(Collectors.toSet());
        for(String k : kp){
            b.append("\n\t\t"+k+" -> ");
            Iterator<GrammarString> alternatives = P.get(k).iterator();
            b.append(alternatives.next());
            while(alternatives.hasNext()){
                b.append("|").append(alternatives.next());
            }
        }
        b.append("\n\t}");
        b.append("\n\tStart: "+this.S);
        b.append("\n\tN_g: "+this.N_g);
        b.append("\n\tN_e: "+this.N_e);
        return b.toString();
    }

    //Shorten rules which have more than 2 GrammarSymbols. (A -> aBbB, B -> c => A -> aB1, B1 -> BB2, B2 -> bB, B -> c)
    public Grammar getShortenedGrammar(){
        Set<String> rules = P.keySet();
        Set<String> NN = new HashSet<>();
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        for(String p : rules) {
            Set<GrammarString> r = P.get(p);
            Set<GrammarString> oldr = new HashSet<>();
            List<GrammarSymbol> str = null;
            int j = 1;
            Optional<GrammarString> op = r.stream().filter(x -> x.getSymbols().size() > 2).findFirst();
            if (op.isPresent()) {//has at least one alternative with 2 or more symbols
                str = op.get().getSymbols();
                List<GrammarSymbol> finalStr = str;
                r = r.stream().filter(x -> !x.getSymbols().equals(finalStr)).collect(Collectors.toSet());//r its all except first alternative which is scanning now
                //if (str.size() > 2) {//if A1 consists of three or more symbols...
                String pr = p;
                for (int i = 0; i < str.size() - 2; i++) {
                    GrammarString n = new GrammarString();
                    n.addSymbol(str.get(i));
                    n.addSymbol(new GrammarSymbol('n', p + j));
                    NN.add(p + j);
                    Set<GrammarString> nbody = new HashSet<>();
                    nbody.add(n);
                    newP.put(pr, nbody);
                    pr = p + j;
                    j++;
                }
                GrammarString last = new GrammarString();
                last.addSymbol(str.get(j - 1));
                last.addSymbol(str.get(j));
                NN.add(pr);
                Set<GrammarString> nbody = new HashSet<>();
                nbody.add(last);
                newP.put(pr, nbody);
                //}//end scaning first alternative. (else add oldr alternative)
                Iterator<GrammarString> it = r.iterator();
                while (it.hasNext()) {//scanning other alternatives.
                    //for (int k = 0; k < r.size(); k++) {
                    int str2_idx = 1;
                    GrammarString rk = it.next();
                    List<GrammarSymbol> str2 = rk.getSymbols();//r.get(k).getSymbols();
                    Set<GrammarString> nbody2 = newP.get(p);
                    if (str2.size() > 2) {
                        GrammarString sn = new GrammarString();
                        sn.addSymbol(str2.get(0));
                        sn.addSymbol(new GrammarSymbol('n', p + j));
                        nbody2.add(sn);
                        nbody2 = new HashSet<>();
                        String pr2 = p + j;
                        NN.add(pr2);
                        j++;
                        str2_idx++;
                        for (int i = 1; i < str2.size() - 2; i++) {
                            GrammarString n = new GrammarString();
                            n.addSymbol(str2.get(i));
                            n.addSymbol(new GrammarSymbol('n', p + j));
                            nbody2.add(n);
                            newP.put(pr2, nbody2);
                            nbody2 = new HashSet<>();
                            pr2 = p + j;
                            NN.add(pr2);
                            j++;
                            str2_idx++;
                        }
                        GrammarString last2 = new GrammarString();
                        last2.addSymbol(str2.get(str2_idx - 1));
                        last2.addSymbol(str2.get(str2_idx));
                        nbody2.add(last2);
                        newP.put(pr2, nbody2);
                    } else {
                        oldr.add(rk);
                    }
                }//end scaning alternatives.
            } else {
                oldr.addAll(r);//r.get(0);
            }
            Set<GrammarString> fl = newP.get(p);
            if(fl != null){
                fl.addAll(oldr);//if some alternatives were short. (they are not producing new rules)
            }
            else{
                newP.put(p,oldr);//if all alternatives were short.
            }
            NN.add(p);
        }
        return new Grammar(this.T,NN,newP,this.S,this.E,this.lex_rules);
    }


    public Grammar getNonEmptyWordsGrammar(){
        Set<String> rules = P.keySet();
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        String newS = this.S;
        Set<String> NN = new HashSet<>();

        //Check whether S -> eps. (ifTrue then add Rule S' -> S | eps).
        Set<GrammarString> S = this.P.get(this.S);
        for(GrammarString al : S){
            List<GrammarSymbol> str = al.getSymbols();
            if( (str.size() == 1 && str.get(0).getVal().equals(this.E)) || isEmptyString(al)){
                newS = newS+"\'";
                GrammarString al1 = new GrammarString();
                GrammarString al2 = new GrammarString();
                al1.addSymbol(new GrammarSymbol('n',this.S));
                al2.addSymbol(new GrammarSymbol('t',this.E));
                Set<GrammarString> srbody = new HashSet<>();
                srbody.add(al1);
                srbody.add(al2);
                newP.put(newS,srbody);
                NN.add(newS);
                break;
            }

        }

        //Generate new set of rules.
        for (String p : rules) {//for each production.
            Set<GrammarString> rule = P.get(p);
            Set<GrammarString> nbody = new HashSet<>();
            for(GrammarString r : rule){
                Set<GrammarString> rbody = getAllESubsetOf(r);
                nbody.addAll(rbody);
            }
            if(nbody.size() > 0) {
                newP.put(p, nbody);
                NN.add(p);
            }
        }
        return new Grammar(this.T,NN,newP,newS,this.E,this.lex_rules);
    }

    //Get all subsets of rule which consists of combinations of N_e.
    private Set<GrammarString> getAllESubsetOf(GrammarString rule){
        Set<GrammarString> result = new HashSet<>();
        LinkedStack<GrammarString> S = new LinkedStack<>();
        S.push(rule);
        while(!S.isEmpty()) {
            GrammarString i = S.top();
            S.pop();
            int k = getERulesFor(i);
            //Do not add rules A -> empty (A -> eps).
            if(k == 0 && i.getSymbols().size() == 1 && i.getSymbols().get(0).getVal().equals(this.E))
                continue;
            else if(k == 1 && i.getSymbols().size() == 1){
                result.add(i);
                continue;
            }
            result.add(i);//add new combo into set.
            ArrayList<Integer> passed = new ArrayList<>();
            while (k > 0) {//split on combinations.
                GrammarString nr = new GrammarString();
                boolean flag = true;
                int j1 = 0;
                for (GrammarSymbol sym : i.getSymbols()){
                    if(flag && N_e.contains(sym.getVal()) && !passed.contains(j1)){
                        flag = false;
                        passed.add(j1);
                        j1++;
                        continue;
                    }
                    GrammarSymbol s = new GrammarSymbol(sym.getType(),sym.getVal());
                    nr.addSymbol(s);
                    j1++;
                }
                //do not add rules A -> empty. (A -> eps)
                if(nr.getSymbols().size() == 1 && nr.getSymbols().get(0).getType() == 't' && nr.getSymbols().get(0).getVal().equals(this.E)){
                    k++;
                    continue;
                }
                S.push(nr);
                k--;
            }
        }
        return result;
    }

    //for grammarString count N_e non-terminals.
    public int getERulesFor(GrammarString rule){
        List<GrammarSymbol> l = rule.getSymbols();
        int i = 0;
        for(GrammarSymbol s : l){
            if(N_e.contains(s.getVal()))
                i++;
        }
        return i;
    }

    private boolean isEmptyString(GrammarString r){
        List<GrammarSymbol> l = r.getSymbols();
        for(GrammarSymbol s  : l){
            if(s.getType() == 't' && s.getVal().equals(this.E))
                continue;
            if(!N_e.contains(s.getVal()))
                return false;
        }
        return true;
    }

    //Get indexed grammar where each non-terminal is ordered. (by specifying unique number begining with start production S)
    //Example: S -> aSbS | bSaS | e => S1 -> aS1bS1 | bS1aS1 | e.
    public Grammar getIndexedGrammar(){
        Map<String,String> nNames = new HashMap<>();
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        Set<String> NN = new HashSet<>();
        int idx = 2;
        LinkedStack<String> pr = new LinkedStack<>();
        pr.push(this.S);
        NN.add(this.S+"_1");
        nNames.put(this.S,this.S+"_1");
        String start = this.S+"_1";
        while(!pr.isEmpty()){
            String p = pr.top();
            String np = nNames.get(p);
            pr.pop();
            Set<GrammarString> alts = P.get(p);
            Set<GrammarString> nalts = new HashSet<>();
            for(GrammarString body : alts){
                GrammarString nb = new GrammarString();
                List<GrammarSymbol> symbols = body.getSymbols();
                for(GrammarSymbol symbol : symbols){
                    if(symbol.getType() == 't') {
                        nb.addSymbol(new GrammarSymbol('t',symbol.getVal()));
                        continue;
                    }
                    String name = nNames.get(symbol.getVal());
                    if(name != null){
                        nb.addSymbol(new GrammarSymbol('n',name));
                    }
                    else{
                        pr.push(symbol.getVal());
                        nNames.put(symbol.getVal(),symbol.getVal()+"_"+idx);
                        nb.addSymbol(new GrammarSymbol('n',symbol.getVal()+"_"+idx));
                        NN.add(symbol.getVal()+"_"+idx);
                        idx++;
                    }
                }
                nalts.add(nb);
            }
            newP.put(np,nalts);
        }
        return new Grammar(this.T,NN,newP,start,this.E,lex_rules);
    }

    //Given production with name n.
    //Produce list of the productions names which are equal to production n.
    private List<String> equalRules(String n){
        List<String> altNames = new LinkedList<>();
        Set<String> ps = P.keySet();
        Set<GrammarString> r1 = P.get(n);
        altNames.add(n);
        for(String p : ps){
            if(p.equals(n))
                continue;
            Set<GrammarString> r2 = P.get(p);
            if(r1.equals(r2))
                altNames.add(p);
        }
        return altNames;
    }

    public Grammar getGrammarWithoutEqualRules(){
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        Set<String> NN = new HashSet<>();
        Set<String> ps = P.keySet();
        for(String p : ps){
            Set<GrammarString> ns = new HashSet<>();
            Set<GrammarString> alts = P.get(p);
            for(GrammarString rule : alts){
                GrammarString nrule = new GrammarString();
                for(GrammarSymbol sym : rule.getSymbols()){
                    GrammarSymbol nsym = new GrammarSymbol(sym.getType(),sym.getVal());
                    if(sym.getType() == 'n'){
                        List<String> eNames = equalRules(sym.getVal());
                        if(eNames.size() > 1){
                            eNames.sort((x,y)->{if(x.length() > y.length()) return 1; else if(x.length() < y.length()) return -1; else return 0;});
                            nsym = new GrammarSymbol(sym.getType(),eNames.get(0));
                        }
                        NN.add(nsym.getVal());
                    }
                    nrule.addSymbol(nsym);
                }
                ns.add(nrule);
            }
            newP.put(p,ns);
        }
        return new Grammar(T,NN,newP,this.S,this.E,lex_rules).deleteUselessSymbols();
    }

    //ELIMINATE LEFT RECURSION. (all type)
    public static Grammar deleteLeftRecursion(Grammar G){
        G = G.deleteUselessSymbols();
        G = G.getNonEmptyWordsGrammar();
        G = G.getNonCycledGrammar();
        G = G.getIndexedGrammar();
        int n = G.getNonTerminals().size();
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        Set<String> NN = new HashSet<>();
        List<String> N = ColUtils.fromSet(G.N);
        N.sort((x,y) -> {
            int x_i = Integer.parseInt(x.split("_")[1]);
            int y_i = Integer.parseInt(y.split("_")[1]);
            return Integer.compare(x_i, y_i);
        });
        //System.out.println(N);
        for(int i = 1; i <= n; i++){
            String Ai = N.get(i - 1);//S_1.
            for(int j = 1; j < i; j++){
                String Aj = N.get(j - 1);
                Set<GrammarString> A_j = G.P.get(Aj);
                Set<GrammarString> A_i = replaceAlternativesWithFirstN(G.P.get(Ai),A_j,Aj);//replace A_i -> bA_jb to A_i -> bal1b|bal2b|bal3b where  A_j -> al1|al2|al3
                //System.out.println(A_i);
                G.P.put(Ai,A_i);//add replaced rule A_i -> bA_jb.
            }
            Set<GrammarString> A_i = G.P.get(Ai);
            //System.out.println(A_i);
            Set<GrammarString> rb1 = new HashSet<>();
            Set<GrammarString> lb1 = new HashSet<>();
            boolean flag = false;
            if(!hasImmediateLeftRecursion(A_i,Ai)){//each alternative has no immediate left-recursion.
                newP.put(Ai,A_i);
                NN.add(Ai);
                continue;
            }
            for(GrammarString str  : A_i){
                int k = 0;
                List<GrammarSymbol> symbols = str.getSymbols();
                if(symbols.size() > 1 && symbols.get(0).getVal().equals(Ai)){//immediate recursion.
                    GrammarString g = new GrammarString(new ArrayList<>(str.getSymbols().subList(1,symbols.size())));//do not add first symbol.
                    g.addSymbol(new GrammarSymbol('n',Ai+"\'"));//add new symbol.
                    lb1.add(g);//add new sequence alpha A' for A -> A alpha. => A -> alpha A'
                    lb1.add(new GrammarString(new ArrayList<>(str.getSymbols().subList(1,symbols.size()))));//add alpha sequence => A -> alpha A' | alpha
                    flag = true;
                }
                else { //there is a beta sequence.
                    GrammarString g = new GrammarString(new ArrayList<>(str.getSymbols()));
                    g.addSymbol(new GrammarSymbol('n',Ai+"\'"));
                    rb1.add(g);//add new sequence beta A' for A -> beta  WHERE beta doesn't begin with A.
                    rb1.add(str);//add old sequence beta
                }
            }
            newP.put(Ai,rb1);
            NN.add(Ai);
            newP.put(Ai+"\'",lb1);
            NN.add(Ai+"\'");
        }
        return new Grammar(G.T,NN,newP,G.getStart(),G.E,G.getLexicalRules());
    }


    //Replace for all alternatives in al1 first symbol with name N (non-term) with alternatives contain body of N.
    //If N also consists of several alternatives then add them as new alternatives.
    //Example A ->  N, beta, N -> y1 | y2 ... | yn
    // => A -> y1, beta | y2, beta | ... | yn, beta.
    private static Set<GrammarString> replaceAlternativesWithFirstN(Set<GrammarString> al1, Set<GrammarString> al2,String N){
        List<GrammarString> it =  ColUtils.fromSet(al2);
        Set<GrammarString> result = new HashSet<>();
        for(GrammarString s  :al1) {
            GrammarString r = new GrammarString();
            if(s.getSymbols().get(0).getVal().equals(N)){
                List<GrammarSymbol> suffix = s.getSymbols().subList(1,s.getSymbols().size());
                int k = 0;
                while(k < it.size()){
                    r.getSymbols().addAll(it.get(k).getSymbols());
                    r.getSymbols().addAll(suffix);
                    result.add(new GrammarString(new ArrayList<>(r.getSymbols())));//S_1d -> a,b,A_2a
                    r = new GrammarString();//only copy! not reference!
                    k++;
                }
            }
            else{
                r.getSymbols().addAll(s.getSymbols());
                result.add(r);
            }
        }
        return result;
    }

    //Replace for all alternatives in al1 all symbol with name N (non-term) with alternatives contain body of N.
    //If N also consists of several alternatives then add them as new alternatives.
    //Example A -> alpha, N, beta, N -> y1 | y2 ... | yn
    // => A -> alpha, y1, beta | alpha, y2, beta | ... | alpha, yn, beta.
    private static Set<GrammarString> replaceAlternativesWithN(Set<GrammarString> al1, Set<GrammarString> al2,String N){
        List<GrammarString> it =  ColUtils.fromSet(al2);
        Set<GrammarString> result = new HashSet<>();
        List<GrammarString> newalts = null;
        for(GrammarString s  :al1){
            newalts = new ArrayList<>();
            GrammarString r = new GrammarString();
            for(GrammarSymbol g : s.getSymbols()){
                if(g.getVal().equals(N)){
                    List<GrammarSymbol> prefix = new ArrayList<>();
                    prefix.addAll(r.getSymbols());//save prefix.
                    int k = 0;
                    while(k < it.size()){
                        r.getSymbols().addAll(it.get(k).getSymbols());
                        newalts.add(new GrammarString(new ArrayList<>(r.getSymbols())));//S_1d -> a,b,A_2a
                        r = new GrammarString(new ArrayList<>(prefix.subList(0,prefix.size())));//only copy! not reference!
                        k++;
                    }
                }
                else{
                    if(newalts.size() > 0){
                        for(int kk = 0; kk < newalts.size(); kk++){
                            GrammarString rk = newalts.get(kk);
                            rk.addSymbol(new GrammarSymbol(g.getType(),g.getVal()));
                        }//if were added alternatives from A_j continue new alternatives with symbols from string s following after symbol A_j.
                    }
                    else
                        r.addSymbol(new GrammarSymbol(g.getType(),g.getVal()));
                }
            }
            if(newalts.size() > 0)
                result.addAll(newalts);
            else
                result.add(r);
        }
        return result;
    }


    private static boolean hasImmediateLeftRecursion(Set<GrammarString> s,String P){
        for(GrammarString body : s){
            if(body.getSymbols().get(0).getVal().equals(P))
                return true;
        }
        return false;
    }

    //Algorithm 2.8
    public Grammar deleteNonReachableSymbols(){
        Set<String> V_0 = new HashSet<>();
        Set<String> V_i = new HashSet<>();
        LinkedStack<Set<String>> S = new LinkedStack<>();
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        V_0.add(this.S);
        S.push(V_0);
        while(true){
            V_0 = S.top();//i++
            V_i.addAll(V_0);
            for(String p : V_0){
                Set<GrammarString> a = P.get(p);//get all grammar strings with rule p.
                if(a == null)
                    continue;
                Set<GrammarString> np = new HashSet<>();
                for (GrammarString alpha : a) {//for each alternative
                    List<GrammarSymbol> symbols = alpha.getSymbols();
                    GrammarString g = new GrammarString();
                    for (GrammarSymbol s : symbols) {//FOR symbol Xi in A -> X1...XN for A in N_i-1
                        V_i.add(s.getVal());
                        g.addSymbol(s);
                    }
                    np.add(g);
                }
                if(N.contains(p)){
                    newP.put(p,np);
                }
            }
            S.pop();
            S.push(V_i);
            if(V_i.equals(V_0)) {
                break;
            }
            V_i = new HashSet<>();
        }
        Set<String> NN = new HashSet<>(V_i);
        NN.retainAll(N);
        Set<String> NT = new HashSet<>(V_i);
        NT.retainAll(T);

        return new Grammar(NT,NN,newP,this.S,this.E,lex_rules);
    }

    //Algorithm 2.9
    public Grammar deleteUselessSymbols(){
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        Set<String> NN = new HashSet<>(N);
        NN.retainAll(this.N_g);
        Set<String> ps = P.keySet();
        for(String p : ps){
            Set<GrammarString> bodies = P.get(p);
            Set<GrammarString> nbodies = new HashSet<>();
            for(GrammarString str : bodies){
                boolean f = true;
                GrammarString g = new GrammarString();
                for(GrammarSymbol s : str.getSymbols()){
                    if(s.getType() != 't' && !NN.contains(s.getVal())){
                        f = false;
                        break;
                    }
                    g.addSymbol(new GrammarSymbol(s.getType(),s.getVal()));
                }
                if(f){
                    nbodies.add(g);
                }
            }
            if(nbodies.size() > 0)
                newP.put(p,nbodies);
        }
        Grammar NG = new Grammar(this.T,NN,newP,this.S,this.E,lex_rules);
        return NG.deleteNonReachableSymbols();
    }

    public Set<String> compute_allGeneratedN(String N){
        Set<String> V_0 = new HashSet<>();
        Set<String> V_i = new HashSet<>();
        LinkedStack<Set<String>> S = new LinkedStack<>();
        V_0.add(N);
        S.push(V_0);
        while(true){
            V_0 = S.top();//i++
            V_i.addAll(V_0);
            for(String p : V_0){
                Set<GrammarString> a = P.get(p);//get all grammar strings with rule p.
                if(a == null)
                    continue;
                Set<GrammarString> np = new HashSet<>();
                for (GrammarString alpha : a) {//for each alternative
                    if(alpha.getSymbols().size() == 1 && alpha.getSymbols().get(0).getType() != 't')
                        V_i.add(alpha.getSymbols().get(0).getVal());
                }
            }
            S.pop();
            S.push(V_i);
            if(V_i.equals(V_0)) {
                break;
            }
            V_i = new HashSet<>();
        }
        return V_i;
    }

    //Algorithm 2.11  Delete rules A -> B, where A,B are non-terminals and length of the body == 1.
    public Grammar getNonCycledGrammar(){
        Map<String,Set<GrammarString>> newP = new HashMap<>();
        Map<String,Set<String>> NG = new HashMap<>();
        for(String n : N){
            NG.put(n,compute_allGeneratedN(n));
        }
        Set<String> ps = P.keySet();
        for(String p : ps){
            Set<GrammarString> rules = P.get(p);
            Set<String> np = new HashSet<>();
            for(String k : NG.keySet()){
                if(NG.get(k).contains(p))
                    np.add(k);
            }
            for(GrammarString rule : rules){
                //if rule is not like A -> B.
                if( (rule.getSymbols().size() == 1 && rule.getSymbols().get(0).getType() == 't') || rule.getSymbols().size() > 1){
                    GrammarString nalt = new GrammarString(new ArrayList<>(rule.getSymbols()));
                    for(String nprod : np){
                        Set<GrammarString> body = newP.get(nprod);
                        if(body != null){
                            body.add(nalt);
                        }
                        else{
                            body = new HashSet<>();
                            body.add(nalt);
                            newP.put(nprod,body);
                        }
                    }
                }
            }
        }
        return new Grammar(this.T,this.N,newP,this.S,this.E,this.lex_rules);
    }

    public Grammar deleteLeftFactor(){
        Map<String,Set<GrammarString>> newP = new HashMap<>(this.P);
        Set<String> NN = new HashSet<>();
        LinkedStack<String> rules = new LinkedStack<>();
        Map<String,Integer> newRidxs = new HashMap<>();
        Set<String> keys = newP.keySet();
        for(String k : keys) {
            if (!k.equals(this.S))
                rules.push(k);
            newRidxs.put(k,1);
        }
        rules.push(this.S);
        boolean hasEmpty = false;
        //int k = 0;
        while(!rules.isEmpty()){
            String ph = rules.top();
            rules.pop();
            Set<GrammarString> alts = new HashSet<>(newP.get(ph));
            Set<GrammarString> nonPref = nonPreffix(alts);
//            System.out.println("P: "+alts);
            alts.removeAll(nonPref);
//            System.out.println("Y: "+alts);
            GrammarString preffix = null;
            if(alts.size() > 0)
                 preffix = commonPreffix(alts);
            else{
                NN.add(ph);
                continue;
            }
//            System.out.println("Preffix: "+preffix);
            if(preffix.getSymbols().size() > 1 || (preffix.getSymbols().size() == 1 && !preffix.getSymbols().get(0).getVal().equals(this.E))) {
                int pL = preffix.getSymbols().size();
                Set<GrammarString> bodyOfnewTerm = new HashSet<>();
                for(GrammarString r : alts){
                    List<GrammarSymbol> l = r.getSymbols();
                    if(l.subList(0,pL).equals(preffix.getSymbols())) {
                        if (pL == l.size()) {
                            GrammarString empty = new GrammarString();
                            empty.addSymbol(new GrammarSymbol('t', this.E));
                            bodyOfnewTerm.add(empty);
                            hasEmpty = true;
                        } else
                            bodyOfnewTerm.add(new GrammarString(new ArrayList<>(l.subList(pL,l.size()))));
                    }
                    else{
                        nonPref.add(new GrammarString(new ArrayList<>(r.getSymbols())));
                    }
                }
                GrammarString prefRule = new GrammarString(new ArrayList<>(preffix.getSymbols()));
                int id = newRidxs.get(ph);
                String nName = ph+"\'"+id;
                prefRule.addSymbol(new GrammarSymbol('n',nName));
                nonPref.add(prefRule);

//                System.out.println("nonPref: "+nonPref);
//                System.out.println("pref: "+bodyOfnewTerm);
                newP.put(ph,nonPref);
                newP.put(nName,bodyOfnewTerm);
                id++;
                newRidxs.put(ph,id);
                newRidxs.put(nName,1);
                rules.push(ph);
                rules.push(nName);
                NN.add(nName);
            }
            NN.add(ph);
        }
        if(hasEmpty){
            HashSet<String> NT = new HashSet<>(this.T);
            NT.add(this.E);
            NN.addAll(N);
          return new Grammar(NT,NN,newP,this.S,this.E,lex_rules);
           // return new Grammar(NT,NN,newP,this.S,this.E,lex_rules).getNonCycledGrammar().getGrammarWithoutEqualRules();
        }
        NN.addAll(N);
        return new Grammar(this.T,NN,newP,this.S,this.E,lex_rules);//.getNonCycledGrammar().getGrammarWithoutEqualRules();
    }

    private int commonMinLength(Set<GrammarString> prod){
        List<GrammarString> rules = ColUtils.fromSet(prod);
        int minL = rules.get(0).getSymbols().size();
        //Find minimum length of the grammarStr.
        for(int i = 1; i < rules.size();i++){
            if(rules.get(i).getSymbols().size() < minL)
                minL = rules.get(i).getSymbols().size();
        }
        return minL;
    }


    //works only with set of alternatives which have at least pair with common preffix
    //Example: [abc, acb, bac, bcd] -> commonPreffixes: a for [abc,acb],  b for [bac,bcd].
    //It stops when it finds the first commonPreffix. (in previous example it stops after find a)
    public Set<GrammarString> nonPreffix(Set<GrammarString> prod){
        Set<GrammarString> nonF = new HashSet<>();
        List<GrammarString> rules = ColUtils.fromSet(prod);
        for(int i = 0; i < rules.size(); i++){
            boolean flag = true;
            GrammarSymbol si = rules.get(i).getSymbols().get(0);
            for(int j = 0; j < rules.size();j++){
                if(j == i)
                    continue;
                GrammarSymbol sj = rules.get(j).getSymbols().get(0);
                if(si.getVal().equals(sj.getVal())) {
                    flag = false;
                    break;
                }
            }
            if(flag){
                nonF.add(new GrammarString(new ArrayList<>(rules.get(i).getSymbols())));
            }
        }
        return nonF;
    }

    public GrammarString commonPreffix(Set<GrammarString> prod){
        List<GrammarString> r = new ArrayList<>();
        List<GrammarString> rules = ColUtils.fromSet(prod);
        int minL = commonMinLength(prod);
        rules.sort((x,y) -> {
            return Integer.compare(x.getSymbols().size(), y.getSymbols().size());
        });
        boolean flag = true;
        int ca = 1;
        int i = 0;
        GrammarString current = null;
        GrammarString preffix = new GrammarString();
        while(flag && i < minL){
            ca = 1;
            current = new GrammarString(rules.get(0).getSymbols().subList(0,i+1));
            for(int j = 1; j < rules.size();j++){
                GrammarString c = new GrammarString(rules.get(j).getSymbols().subList(0,i+1));
                if(c.equals(current)){
                    ca++;
                }
            }
            if(ca > 1)
                preffix = current;
            else
                flag = false;
            i++;
        }
        return preffix;
    }
}