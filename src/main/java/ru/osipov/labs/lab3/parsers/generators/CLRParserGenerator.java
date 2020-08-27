package ru.osipov.labs.lab3.parsers.generators;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.*;

import java.util.*;

//LR(1) Automaton builder. [Uses LR(1) items]
public class CLRParserGenerator {
    public static LR_1_Automaton buildLRAutomaton(Grammar G) throws Exception {
        Map<Integer, Set<LR1GrammarItem>> C = new TreeMap<>();//canonical items.
        Map<Pair<Integer,String>,Integer> gotoTable = new HashMap<>();//goto function.

        String S0 = G.getStart();
        String S1 = G.getStart()+"\'";
        try {
            G.extendStart(S1);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            throw new Exception("Cannot extend Grammar");
        }
        GrammarString start = new GrammarString();

        Map<String,Set<String>> firstTable = LLParserGenerator.firstTable(Grammar.deleteLeftRecursion(G));
        HashSet<String> EOF = new HashSet<>();
        EOF.add("$");
        firstTable.put("$",EOF);//FIRST($) = { $ };
        Map<String,Set<String>> oF = new HashMap<>();
        for(String k : firstTable.keySet()){
            if(!k.contains("\'")) {
                if(k.contains("_"))
                    oF.put(k.substring(0, k.indexOf('_')), firstTable.get(k));
                else
                    oF.put(k,firstTable.get(k));
            }
        }
        oF.put("$",EOF);
        firstTable = oF;//computed FIRST on non left-recursive grammar with old names of grammar symbols.

//        G = G.getIndexedGrammar();
//        S0 = S0+"_2";
//        S1 = G.getStart();
        start.addSymbol(new GrammarSymbol('n',S0));
        LR1GrammarItem S = new LR1GrammarItem(start,S1,"$");//point [S' -> .S, $]

        Set<String> symbols = new HashSet<>(G.getNonTerminals());
        symbols.addAll(G.getTerminals());
        LinkedStack<Set<LR1GrammarItem>> ST = new LinkedStack<>();
        LinkedStack<Integer> states = new LinkedStack<>();//number of states.
        C.put(0,closure(G,S,firstTable));
        ST.push(C.get(0));
        states.push(0);
        int count = 0;
        int cstate = 0;
        while(!ST.isEmpty() && !states.isEmpty()){
            Set<LR1GrammarItem> old = ST.top();
            ST.pop();
            cstate = states.top();
            states.pop();
            Set<LR1GrammarItem> ns = null;
            for(String s : symbols){
                ns = gotoSet(G,old,s,firstTable);
                if(ns.size() > 0 && !C.containsValue(ns)) {//IF NOT NULL AND NOT EMPTY AND NEW => ADD NEW SET.
                    count++;//new set was discovered.
                    ST.push(ns);
                    states.push(count);
                    gotoTable.put(new Pair<Integer,String>(cstate,s),count);
                    C.put(count,ns);
                }
                else if(ns.size() > 0){
                    for(Map.Entry e: C.entrySet()){
                        if(e.getValue().equals(ns))
                            gotoTable.put(new Pair<Integer,String>(cstate,s), (Integer) e.getKey());
                    }
                }
            }
        }
        System.out.println("Canonical Set of Items was built.");
        return new LR_1_Automaton(G,S0,S1,C,gotoTable,firstTable);
    }



    private static Set<LR1GrammarItem> closure(Grammar G, LR1GrammarItem item, Map<String,Set<String>> firstTable){
        Set<LR1GrammarItem> C = new HashSet<>();
        C.add(item);//initially C = { item };

        LinkedStack<String> ST = new LinkedStack<>();//STACK of closure.

        //Items like [A -> y.] and [A -> a .b C] do not generate new items.
        if(item.getAt() == null || item.getAt().getType() != 'n')
            return C;

        ST.push(item.getAt().getVal());//scan production for .X symbol. [A -> a .X b]
        ArrayList<Pair<String,String>> backtrack = new ArrayList<>();//backtracking for uniqueness.
        String current = null;//current .X symbol.
        LinkedStack<LR1GrammarItem> items = new LinkedStack<>();
        items.push(item);
        String e = G.getEmpty();
        //System.out.println("Kernel: "+item);
        while(!ST.isEmpty()) {
            current = ST.top();
            item = items.top();
            items.pop();
            backtrack.add(new Pair<String,String>(current,item.getTerm()));
            Set<GrammarString> rules = G.getProductions().get(current);
            ST.pop();
            if(rules != null) {
                Set<String> first_b = LLParserGenerator.first(getBetweenPosition(item,item.getPosition() + 1,item.getTerm()),firstTable,e);
                //System.out.println(first_b);
                for(GrammarString r: rules){
                    GrammarSymbol next = r.getSymbols().get(0);
                    //[A -> .A t, b]
                    if(next != null && next.getType() == 'n' && next.getVal().equals(current)){
                         first_b.addAll(LLParserGenerator.first(getBetweenPosition(r,1,item.getTerm()),firstTable,e));
                    }
                }
                for(GrammarString r : rules){
                    GrammarSymbol next = r.getSymbols().get(0);
                    for(String b : first_b){
                        LR1GrammarItem nitem = new LR1GrammarItem(r,current,b);
                        C.add(nitem);
                        if (next != null && next.getType() == 'n' && !backtrack.contains(new Pair<String,String>(next.getVal(),b))) {//if it is a new non-term.
                            ST.push(next.getVal());
                            items.push(nitem);
                        }
                    }
                }
//                for(GrammarString r : rules) {//check its productions. [B -> .y]
//                    LR1GrammarItem ni = null;
//                    GrammarSymbol next = r.getSymbols().get(0);
//                    //INIT GrammarItem which contains [B -> .y]
//
//                    for(String b : first_b) {//FOR b in FIRST(X1 X2) where [A -> alpha .B X1, X2]
//                        LR1GrammarItem nitem = new LR1GrammarItem(r, current, b);
//                        C.add(nitem);//add item [B -> .y, b]
//                        update.add(nitem);
//                        if (next != null && next.getType() == 'n' && !backtrack.contains(next.getVal())) {//if it is a new non-term.
//                            ST.push(next.getVal());
//                            items.push(nitem);
//                        }
//                        else{
//                            ni = nitem;
//                        }
//                    }
//                    if (next != null && next.getType() == 'n' && ni != null){ //may produce other item because its right part may contains new symbols from FIRST.
//                        //EXTEND FIRST AND ADD NEW Items FOR EACH RULE. [B -> .y, nb] WHERE nb IN EXTEND(FIRST)
//                        Set<String> nf = LLParserGenerator.first(getBetweenPosition(ni,ni.getPosition() + 1,ni.getTerm()),firstTable,e);
//                        for(LR1GrammarItem i : update){
//                            for(String bn : nf){
//                                LR1GrammarItem nitem = new LR1GrammarItem(i.getSymbols(),i.getPosition(), current, bn);
//                                C.add(nitem);
//                            }
//                        }
//                        first_b.addAll(nf);
//                    }
//                }
            }
        }
        return C;
    }

    private static Set<LR1GrammarItem> gotoSet(Grammar G, Set<LR1GrammarItem> I, String symbol, Map<String,Set<String>> firstTable){
        Set<LR1GrammarItem> R = new HashSet<>();
        for(LR1GrammarItem point : I){
            if(point.getAt() != null && point.getAt().getVal().equals(symbol)){// Point: [A -> a .symbol b, X]
                LR1GrammarItem ni = new LR1GrammarItem(point.getSymbols(),point.getPosition() + 1,point.getHeader(),point.getTerm());
                R.addAll(closure(G,ni,firstTable));
            }
        }
        return R;
    }

    private static GrammarString getBetweenPosition(GrammarString old, int pos,String t){
        List<GrammarSymbol> ba = new ArrayList<>();
        if(pos < old.getSymbols().size())//IF there something else after .symbol [A -> .symbol X, b]
            ba.addAll(old.getSymbols().subList(pos,old.getSymbols().size()));
        ba.add(new GrammarSymbol('t',t));//FIRST(B a) where a = term
        return new GrammarString(ba);
    }
}
