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
    public Map<Pair<String,String>, GrammarString> getTable(Grammar G){
        HashMap<Pair<String,String>, GrammarString> table = new HashMap<>();

        Map<String,Set<String>> firstTable = firstTable(G);
        Map<String,Set<String>> followTable = followTable(G,firstTable);
        Set<String> ps = G.getProductions().keySet();
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
                        table.put(rec,b);// new GrammarString(new ArrayList<>(b.getSymbols())));
                    }
                }
                else
                    for(String a : first){
                        Pair<String, String> rec = new Pair<>();
                        rec.setV1(p);
                        rec.setV2(a);
                        table.put(rec, b);//new GrammarString(new ArrayList<>(b.getSymbols())));
                    }
            }
        }
        return table;
    }

    //Compute FIRST for each terminal and non-terminal.
    public Map<String,Set<String>> firstTable(Grammar G){
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
                    else{
                        if(res.containsKey(s.getVal())){
                            if(res.get(s.getVal()).contains(G.getEmpty())) {
                                Set<String> without_empty = res.get(s.getVal());
                                without_empty.remove(G.getEmpty());
                                first_i.addAll(without_empty);
                                ec++;//continue scanning string.
                                continue;
                            }
                            first_i.addAll(res.get(s.getVal()));//add all symbols from first(X_i) where X_i is grammar symbol of str.
                            break;
                        }
                        addAll = true;
                        break;
                    }
                }
                if(addAll){
                    S.push(p);

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
    private Set<String> first(GrammarString str, Map<String,Set<String>> firstTable,String eps){
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

    /* NOT WORKED. May be DELETED.
    public Map<String,Set<String>> followTable(Grammar G,Map<String,Set<String>> firstTable){
        List<String> NT = ColUtils.fromSet(G.getNonTerminals());
        String empty = G.getEmpty();
        HashMap<String,Set<String>> res = new HashMap<String,Set<String>>();
        LinkedStack<String> S = new LinkedStack<>();
        for(String N : NT)
            S.push(N);

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
                        Set<String> first = first(subStr,firstTable,empty);
                        first.remove(empty);
                        if(sym.getVal().equals(G.getStart()))
                            first.add("$");
                        res.put(l.get(i).getVal(),first);
                    }
                }
            }
        }
//        for(String k : res.keySet())
//             System.out.println(k+": "+res.get(k));
        for(int i = NT.size() - 1; i >= 0; i--)
            S.push(NT.get(i));
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
                        if(first.contains(empty))
                            first.addAll(res.get(p));
                        first.remove(empty);
                        if(sym.getVal().equals(G.getStart()))
                            first.add("$");
                        res.put(l.get(i).getVal(),first);
                    }
                }
                GrammarSymbol last = l.get(l.size() - 1);
                if(last.getType() != 't'){
                    Set<String> first = res.get(p);
                    res.put(last.getVal(),first);
                }
            }
        }
        return res;
    }*/

    public Map<String,Set<String>> followTable(Grammar G,Map<String,Set<String>> firstTable){
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