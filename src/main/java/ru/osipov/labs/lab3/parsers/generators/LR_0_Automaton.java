package ru.osipov.labs.lab3.parsers.generators;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarItem;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//Contains LR(0) items without symbol.
public class LR_0_Automaton {
    private Map<Integer, Set<GrammarItem>> C;//Canonical items. [LR(0)-items]
    protected Map<Pair<Integer,String>,Integer> gotoTable;//Function GOTO(state,Symbol) = newState.

    //Function ACTION(state,Symbol) = shift\reduce N |a|\err\acc
    //where acc = accept, err = error, shift => add to stack new state
    //reduce => command with two parameters: production header, count of symbols (which will be deleted
    //from stack)
    protected Map<Pair<Integer,String>,String> actionTable;

    //FOLLOW function for each non-term A. returns a set of terms.
    protected Map<String,Set<String>> follow;

    //new Start header for Grammar.
    protected String S;
    //old Start symbol of Grammar.
    protected String S0;


    //ONLY FOR LR_1_Automaton and other subclasses.
    protected LR_0_Automaton(Grammar G,String oldS, String nS,Map<Pair<Integer,String>,Integer> g,Map<String,Set<String>> firstTable){
        this.gotoTable = g;
        this.actionTable = new HashMap<>();
        this.follow = new HashMap<>();
        this.S0 = oldS;
        this.S = nS;
        this.follow = LLParserGenerator.followTable(G,firstTable);
//        for(String fk : fl.keySet()){
//            Set<String> v = fl.get(fk);
//            follow.put(fk.substring(0,fk.indexOf('_')),v);
//        }
    }

    public LR_0_Automaton(Grammar G,String oldS, String ns, Map<Integer,Set<GrammarItem>> C, Map<Pair<Integer,String>,Integer> g, Map<String,Set<String>> firstTable) {
        this.C = C;
        this.gotoTable = g;
        this.actionTable = new HashMap<>();
        this.follow = new HashMap<>();
        this.S0 = oldS;
        this.S = ns;
        Map<String,Set<String>> fl = LLParserGenerator.followTable(G.getIndexedGrammar(),firstTable);
        for(String fk : fl.keySet()){
            Set<String> v = fl.get(fk);
            follow.put(fk.substring(0,fk.indexOf('_')),v);
        }
        System.out.println(follow);
        initActions();
        System.out.println("ACTION and GOTO are built.");
    }

    private void initActions(){
        boolean hasErr = false;
        Set<Integer> keys = C.keySet();
        for(Integer i : keys){//for each set of items C_i in C
            Set<GrammarItem> C_i = C.get(i);
            for(GrammarItem item : C_i){//for each item in C_i
                GrammarSymbol s = item.getAt();
                if(s != null && s.getType() == 't'){//item [A -> a .bc]
                    Pair<Integer,String> k = new Pair<Integer,String>(i,s.getVal());
                    Integer j = gotoTable.getOrDefault(k,-1);
                    if(j != -1 && !actionTable.containsKey(k))//GOTO IS CORRECT AND RECORD IS NOT FILLED?
                        actionTable.put(new Pair<Integer,String>(i,s.getVal()),"s_"+j);
                    else if(j != -1){
                        String c = actionTable.get(k);
                        if(c.charAt(0) != 's') {
                            hasErr = true;
                            System.out.println("Err item from I_"+i+": " + item+" term: "+item.getAt());
                            System.out.println("Ambiguous: "+actionTable.get(k)+" / s_"+j);
                            System.out.println("Conflict detected! (Reduce-Shift) Grammar is not SLR(1)!");
                            System.out.println("Try to resolve as shift.");
                            actionTable.put(k,"s_"+j);
                        }
                        else if(Integer.parseInt(c.substring(2)) != j){
                            hasErr = true;
                            System.out.println("Err item from I"+i+": " + item+"term: "+item.getAt());
                            System.out.println("Ambiguous: "+actionTable.get(k)+" / s_"+j);
                            System.out.println("Conflict detected! (Shift-Shift) Grammar is not SLR(1)!");
                        }
                    }
                    else{
                        actionTable.put(new Pair<Integer,String>(i,s.getVal()),"err");
                    }
                }
                else if (s != null){//item [A -> a .Bc]
                    actionTable.put(new Pair<Integer,String>(i,s.getVal()),"err");
                }
                else {//item like [A -> y.]
                    //item [S' -> S.]
                    if(item.getHeader().equals(S) && item.getSymbols().get(0).getVal().equals(S0)){
                        actionTable.put(new Pair<Integer,String>(i,"$"),"acc");
                        continue;
                    }
                    //item [S' -> y.]
                    else if(item.getHeader().equals(S)){
                        actionTable.put(new Pair<Integer,String>(i,"$"),"err");
                        continue;
                    }
                    Set<String> t = follow.get(item.getHeader());
                    //System.out.println(i+": "+item);
                    for(String term : t){
                        Pair<Integer,String> k = new Pair<Integer,String>(i,term);
                        if(!actionTable.containsKey(k))//RECORD IS NOT FILLED?
                            actionTable.put(k,"r_"+item.getHeader()+":"+item.getSymbols().size());
                        else{
                            hasErr = true;
                            String com = actionTable.get(k);
                            char a = com.charAt(0);
                            String conftype = (a == 's') ? "(Shift-Reduce)" : "(Reduce-Reduce)";
                            System.out.println("Err item from I"+i+": " + item+" term: "+term);
                            System.out.println("Ambiguous: "+com+" / r_"+item.getHeader()+":"+item.getSymbols().size());
                            System.out.println("Conflict detected! "+conftype+" Grammar is not SLR(1)!");
                        }
                    }
                }
            }
        }
    }

    public Map<Integer, Set<GrammarItem>> getItems() {
        return C;
    }

    public Map<Pair<Integer, String>, Integer> getGotoTable() {
        return gotoTable;
    }

    public Map<Pair<Integer, String>, String> getActionTable() {
        return actionTable;
    }

    public String getStart() {
        return S;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Canonical Grammar LR(0) items : {");
        for(Integer i : C.keySet()){
            sb.append("\n\tI").append(i).append(": {");
            for(GrammarItem item : C.get(i)){
                sb.append("\n\t\t").append(item);
            }
            sb.append("\n\t}");
        }
        sb.append("\n}");
        sb.append("\n GOTO: {");
        for(Pair<Integer,String> k : gotoTable.keySet()){
            sb.append("\n\t"+k.getV1()+", "+k.getV2()+" = "+gotoTable.get(k));
        }
        sb.append("\n}");
        return sb.toString();
    }
}
