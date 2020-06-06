package ru.osipov.labs.lab4.parsers.generators;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OPParserGenerator {

    //for reduce step.
    public ReduceIndexer getIndicesOfRules(Grammar G){
        Map<String,Integer> indices = new HashMap<>();
        Map<Integer,String> parents = new HashMap<>();
        Set<String> NT = G.getNonTerminals();
        int i = 1;
        Set<String> P = G.getProductions().keySet();
        for(String N : P) {
            for (GrammarString str : G.getProductions().get(N)) {
                indices.put(str.toString().replaceAll("\\s+",""), i);
                parents.put(i,N);
                i++;
            }
        }
        return new ReduceIndexer(indices,parents);
    }

    public OperatorPresedenceRelations operatorPresedence(Grammar G){
        Set<String> T = G.getTerminals();
        int sz = T.size() + 1;
        char [][] matrix = new char [sz][sz];
        Map<String,Integer> idx = new HashMap<>();
        int i = 0;
        for(String term : T){
            idx.put(term,i);
            i++;
        }
        idx.put("$",i);//A[i,j] = term where i == j.


        String marker = "$";
        Map<String,Set<String>> Lt = G.getLeftTermMap(G.getLMap());
        Map<String,Set<String>> Rt = G.getRightTermMap(G.getRMap());

        Set<String> t1 = Lt.get(G.getStart());//Lt(S)
        Set<String> t2 = Rt.get(G.getStart());//Rt(S)

        System.out.println(G.getStart());
        int j = 0;
        for(String term : t1){
            j = idx.get(term);
            matrix[i][j] = '<';
        }
        j = idx.get("$");
        for(String term : t2){
            i = idx.get(term);
            matrix[i][j] = '>';
        }

        T.add("$");
        Set<String> P = G.getProductions().keySet();

            for (String h : P) {
                Set<GrammarString> rules = G.getProductions().get(h);//get rules with header h.
                for (GrammarString rule : rules) {
                    List<GrammarSymbol> l = rule.getSymbols();
                    if(l.size() == 1)//pass rules like U -> U or U -> a, where U = non-term, a = term.
                        continue;
                    for (int li = 0; li < l.size() - 2; li++) {//scanning body.
                        GrammarSymbol ai = l.get(li);
                        if(ai.getType() == 't'){
                            GrammarSymbol bj = l.get(li + 1);
                            if(bj.getType() == 'n'){//located rule like aU where a = term, U = non-term
                                Set<String> Li = Lt.get(bj.getVal());//Lt(U)
                                int idx1 = idx.get(ai.getVal());
                                for(String c : Li){
                                    int idx2 = idx.get(c);
                                    matrix[idx1][idx2] = '<';
                                }
                                bj = l.get(li + 2);
                                if(bj.getType() == 't'){//located rule like aUb where a,b = terms, U = non-term.
                                    idx1 = idx.get(ai.getVal());//index a
                                    int idx2 = idx.get(bj.getVal());//index b
                                    matrix[idx1][idx2] = '=';
                                    ai = l.get(li + 1);//return U
                                    Set<String> Ri = Rt.get(ai.getVal());//Rt(U)
                                    for(String c : Ri){//for rule like Ub
                                        idx1 = idx.get(c);
                                        matrix[idx1][idx2] = '>';
                                    }
                                }
                            }
                            else if(bj.getType() == 't'){//located rule ab.
                                int idx1 = idx.get(ai.getVal());
                                int idx2 = idx.get(bj.getVal());
                                matrix[idx1][idx2] = '=';
                            }
                        }
                        //looking for rules Uai where U = non-term, ai = term.
                        else if(ai.getType() == 'n'){
                            GrammarSymbol bj = l.get(li + 1);
                            if(bj.getType() == 't'){//located rule Ua where U = non-term, a = term.
                                Set<String> Ri = Rt.get(ai.getVal());//Rt(U).
                                int idx2 = idx.get(bj.getVal());
                                int idx1 = 0;
                                for(String c : Ri){
                                    idx1 = idx.get(c);
                                    matrix[idx1][idx2] = '>';
                                }
                            }
                        }
                    }
                    //Check last two symbols of body.
                    GrammarSymbol a = l.get(l.size() - 2);
                    GrammarSymbol b = l.get(l.size() - 1);
                    if(a.getType() == 't' && b.getType() == 't'){
                        int idx1 = idx.get(a.getVal());
                        int idx2 = idx.get(b.getVal());
                        matrix[idx1][idx2] = '=';
                    }
                    else if(a.getType() == 'n' && b.getType() == 't'){
                        Set<String> Ri = Rt.get(a.getVal());
                        int idx2 = idx.get(b.getVal());
                        int idx1 = 0;
                        for(String c : Ri){
                            idx1 = idx.get(c);
                            matrix[idx1][idx2] = '>';
                        }
                    }
                    else if(a.getType() == 't' && b.getType() == 'n'){
                        Set<String> Li = Lt.get(b.getVal());
                        int idx1 = idx.get(a.getVal());
                        int idx2 = 0;
                        for(String c : Li){
                            idx2 = idx.get(c);
                            matrix[idx1][idx2] = '<';
                        }
                    }
                }
            }
        return new OperatorPresedenceRelations(matrix,idx);
    }
}
