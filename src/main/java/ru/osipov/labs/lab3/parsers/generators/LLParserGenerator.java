package ru.osipov.labs.lab3.parsers.generators;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.structures.observable.ObservableHashSet;
import ru.osipov.labs.lab1.utils.ColUtils;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;

import java.util.*;

public class LLParserGenerator {
    public static Map<Pair<String,String>, GrammarString> getTable(Grammar G){
        HashMap<Pair<String,String>, GrammarString> table = new HashMap<>();

        Map<String,Set<String>> firstTable = firstTable(G);
        Map<String,Set<String>> followTable = followTable(G,firstTable);
        Set<String> ps = G.getProductions().keySet();
        boolean hasErr = false;
        String empty = G.getEmpty();
        for(String p : ps){
            Set<GrammarString> bodies = G.getProductions().get(p);
            for(GrammarString b: bodies){
                Set<String> first = first(b,firstTable,empty);
                if(first.contains(empty)) {
                    Set<String> follow = followTable.get(p);
                    for (String f : follow) {
                        Pair<String, String> rec = new Pair<>();
                        rec.setV1(p);
                        rec.setV2(f);
                        if(table.get(rec) != null) {
                            hasErr = true;
                            System.out.println("Grammar is not LL(1)!");
                        }
                        table.put(rec,b);// new GrammarString(new ArrayList<>(b.getSymbols())));
                    }
                }
                else
                    for(String a : first){
                        Pair<String, String> rec = new Pair<>();
                        rec.setV1(p);
                        rec.setV2(a);
                        if(table.get(rec) != null){
                            hasErr = true;
                            System.out.println("Grammar is not LL(1)!");
                        }
                        table.put(rec, b);//new GrammarString(new ArrayList<>(b.getSymbols())));
                    }
            }
        }
        return table;
    }

    //Compute FIRST for each terminal and non-terminal.
    //WARNING: Only for Grammars without Left-recursion!
    //You must eliminate left-recursion in Grammar G.
    public static Map<String,Set<String>> firstTable(Grammar G){
        Set<String> T = G.getTerminals();
        Set<String> NT = G.getNonTerminals();
        HashMap<String,Set<String>> res = new HashMap<String,Set<String>>();
        LinkedStack<String> S = new LinkedStack<>();
        for(String t : T){ //for each terminal put first(t) = t.
            HashSet<String> f = new HashSet<>();
            f.add(t);
            res.put(t,f);
        }
        for(String n : NT){
            S.push(n);
        }
        S.push(G.getStart());
        M1: while(!S.isEmpty()){//compute first for each nonTerminal p.
            Set<String> first_i = new HashSet<>();
            String p = S.top();
            S.pop();
            if(res.containsKey(p))//computed.
                continue;
            Set<GrammarString> bodies = G.getProductions().get(p);
            for(GrammarString str : bodies){
                int l = str.getSymbols().size();
                int ec = 0;
                boolean addAll = false;
                for(GrammarSymbol s : str.getSymbols()){
                    if(s.getType() == 't'){
                        if(s.getVal().equals(G.getEmpty()))
                            ec++;//continue scanning string.
                        else {
                            first_i.add(s.getVal());
                            break;//end scanning after first terminal.
                        }
                    }
                    else{//non-terminal.
                        if(res.containsKey(s.getVal())){//if it's already computed.
                            if(res.get(s.getVal()).contains(G.getEmpty())) {//if FIRST(X) contains empty.
                                Set<String> without_empty =  new HashSet<>(res.get(s.getVal()));//add all except empty.
                                without_empty.remove(G.getEmpty());
                                first_i.addAll(without_empty);
                                ec++;//continue scanning string.
                                continue;
                            }
                            first_i.addAll(res.get(s.getVal()));//add all symbols from first(X_i) where X_i is grammar symbol of str.
                            break;//stop scanning body because X_i doesn't contains empty.
                        }
                        addAll = true;//it is a non-computed, new non-term.
                        break;//suspend scanning until FIRST(X_i) is not computed.
                    }
                }
                if(addAll){//non-computed, new non-term X was discovered while scanning body.
                    S.push(p);//save non-computed current non-term into stack (pause).
                    List<GrammarSymbol> ns = str.getSymbols();
                    for(int i = ns.size() - 1; i >= 0; i--){
                        if(ns.get(i).getType() == 'n')
                            S.push(ns.get(i).getVal());
                    }
                    continue M1;
                }
                else{
                    if(ec == l)//add empty if and only if all grammar symbols X in str contains eps in FIRST(X)
                        first_i.add(G.getEmpty());
                }
            }
            res.put(p,first_i);
        }
        return res;
    }

    //Compute FIRST for GrammarString str. (list of GrammarSymbols)
    public static Set<String> first(GrammarString str, Map<String,Set<String>> firstTable,String eps){
        Set<String> res = new HashSet<>();
        int ec = 0;
        for(GrammarSymbol s : str.getSymbols()){
            HashSet<String> ans = new HashSet<String>(firstTable.get(s.getVal()));
            if(!ans.contains(eps)) {
                res.addAll(ans);
                break;
            }
            else{
                ans.remove(eps);
                res.addAll(ans);//add all symbols except eps and continue.
                ec++;
            }
        }
        if(ec == str.getSymbols().size())
            res.add(eps);
        return res;
    }


    //WARNING: Only for Grammars without Left-recursion!
    //You must eliminate left-recursion in Grammar G.
    public static Map<String,Set<String>> followTable(Grammar G,Map<String,Set<String>> firstTable){
        List<String> NT = ColUtils.fromSet(G.getNonTerminals());
        String empty = G.getEmpty();
        HashMap<String,Set<String>> res = new HashMap<String,Set<String>>();
        LinkedStack<String> S = new LinkedStack<>();
        for(String N : NT) {
            S.push(N);
            res.put(N,new ObservableHashSet<String>());
        }
        res.get(G.getStart()).add("$");//add $ to FOLLOW(S) where S = start symbol of G.
        while(!S.isEmpty()){
            String p = S.top();
            S.pop();
            Set<GrammarString> bodies = G.getProductions().get(p);
            for(GrammarString str : bodies){
                List<GrammarSymbol> l = str.getSymbols();
                for(int i = 0; i < l.size()-1;i++){
                    GrammarSymbol sym = l.get(i);
                    if(sym.getType() != 't'){
                        GrammarString subStr = new GrammarString(new ArrayList<>(l.subList(i+1,l.size())));
                        Set<String> first =  first(subStr,firstTable,empty);
                        if(first.contains(empty)) {
                            ((ObservableHashSet<String>) res.get(p)).attach((ObservableHashSet<String>) res.get(l.get(i).getVal()));
                            first.addAll(res.get(p));
                        }
                        first.remove(empty);
//                        if(sym.getVal().equals(G.getStart()))
//                            first.add("$");
                        res.get(l.get(i).getVal()).addAll(first);
                        //res.put(l.get(i).getVal(),first);
                    }
                }
                GrammarSymbol last = l.get(l.size() - 1);
                if(last.getType() != 't'){
                    Set<String> first = res.get(p);
                    ((ObservableHashSet<String>) first).attach((ObservableHashSet<String>) res.get(last.getVal()));
                    //res.put(last.getVal(),first);
                }
            }
        }
        return res;
    }
}