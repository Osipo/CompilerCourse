package ru.osipov.labs.lab3.parsers.generators;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarItem;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab2.grammars.LR1GrammarItem;
import ru.osipov.labs.lab3.parsers.generators.LR_0_Automaton;

import java.util.Map;
import java.util.Set;
//Contains LR(1) items with symbol (T  U  {$}).
//Inherits from LR_0_Automaton:
//      GOTO table with oldStart (S0) newStart (S1), FOLLOW set and empty actionTable
public class LR_1_Automaton extends LR_0_Automaton {
    private Map<String,Set<String>> firstTable;
    private Map<Integer,Set<LR1GrammarItem>> C1;
    //then follow, S0, S, gotoTable, actionTable.

    public LR_1_Automaton(Grammar G,String oldS, String ns, Map<Integer,Set<LR1GrammarItem>> C1, Map<Pair<Integer,String>,Integer> g, Map<String, Set<String>> firstTable){
        super(G,oldS,ns,g,firstTable);
        this.firstTable = firstTable;
        this.C1 = C1;
        initActions();
        System.out.println("ACTION and GOTO are built.");
    }

    private void initActions(){
        Set<Integer> keys = C1.keySet();
        for(Integer i : keys){//for each set of items C1_i in C1
            Set<LR1GrammarItem> C1_i = C1.get(i);
            for(LR1GrammarItem item : C1_i){//for each item in C1_i
                GrammarSymbol s = item.getAt();
                if(s != null && s.getType() == 't'){//item [A -> a .bC1]
                    Pair<Integer,String> k = new Pair<Integer,String>(i,s.getVal());
                    Integer j = gotoTable.getOrDefault(k,-1);
                    if(j != -1 && !actionTable.containsKey(k))//GOTO IS CORRECT AND RECORD IS NOT FILLED?
                        actionTable.put(new Pair<Integer,String>(i,s.getVal()),"s_"+j);
                    else if(j != -1){
                        String C1 = actionTable.get(k);
                        if(C1.charAt(0) != 's') {
                            hasErr = true;
                            System.out.println("Error:");
                            System.out.println("\tErr item from I"+i+": " + item+"term: "+item.getAt());
                            System.out.println("\tAmbiguous: "+actionTable.get(k)+" / s_"+j);
                            System.out.println("\tConflict detected! (Reduce-Shift) Grammar is not LR(1)!");
                            System.out.println("\tTry to resolve as Shift.");
                            actionTable.put(k,"s_"+j);
                        }
                        else if(Integer.parseInt(C1.substring(2)) != j){//not the same.
                            hasErr = true;
                            System.out.println("Error.");
                            System.out.println("\tErr item from I"+i+": " + item+"term: "+item.getAt());
                            System.out.println("\tAmbiguous: "+actionTable.get(k)+" / s_"+j);
                            System.out.println("\tConflict detected! (Shift-Shift) Grammar is not LR(1)!");
                        }
                    }
                    else{
                        actionTable.put(new Pair<Integer,String>(i,s.getVal()),"err");
                    }
                }
                else if (s != null){//item [A -> a .BC1]
                    actionTable.put(new Pair<Integer,String>(i,s.getVal()),"err");
                }
                else {//item like [A -> y.]
                    //item [S' -> S., $]
                    if(item.getHeader().equals(S) && item.getSymbols().get(0).getVal().equals(S0) && item.getTerm().equals("$")){
                        actionTable.put(new Pair<Integer,String>(i,"$"),"acc");
                        continue;
                    }
                    //item [S' -> y.]
                    else if(item.getHeader().equals(S)){
                        actionTable.put(new Pair<Integer,String>(i,item.getTerm()),"err");
                        continue;
                    }
                    Pair<Integer,String> k = new Pair<Integer,String>(i,item.getTerm());
                    if(!actionTable.containsKey(k))//RECORD IS NOT FILLED?
                        actionTable.put(k,"r_"+item.getHeader()+":"+item.getSymbols().size());
                    else{
                        hasErr = true;
                        String com = actionTable.get(k);
                        char a = com.charAt(0);
                        String conftype = (a == 's') ? "(Shift-Reduce)" : "(Reduce-Reduce)";
                        System.out.println("Error.");
                        System.out.println("\tErr item from I_"+i+": " + item+" term: "+item.getTerm());
                        System.out.println("\tAmbiguous: "+com+" / r_"+item.getHeader()+":"+item.getSymbols().size());
                        System.out.println("\tConflict detected! "+conftype+" Grammar is not LR(1)!");
                    }
                }
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Canonical Grammar LR(1) items : {");
        for(Integer i : C1.keySet()){
            sb.append("\n\tI").append(i).append(": {");
            for(GrammarItem item : C1.get(i)){
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

    public Map<Integer, Set<LR1GrammarItem>> getLR1Items(){
        return C1;
    }
}
