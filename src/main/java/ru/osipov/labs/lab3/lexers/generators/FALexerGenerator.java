package ru.osipov.labs.lab3.lexers.generators;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import ru.osipov.labs.lab1.Main;
import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.structures.automats.NFA;
import ru.osipov.labs.lab1.structures.graphs.Edge;
import ru.osipov.labs.lab1.structures.graphs.Elem;
import ru.osipov.labs.lab1.structures.graphs.Vertex;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.utils.RegexRPNParser;
import ru.osipov.labs.lab2.grammars.Grammar;

import java.util.*;

public class FALexerGenerator {
    public CNFA buildNFA(Grammar G){
        Map<String, List<String>> rules = G.getLexicalRules();
        HashSet<Character> alpha = new HashSet<>();
        RegexRPNParser parser = new RegexRPNParser();
        Vertex vs = new Vertex("1");
        Vertex nf = new Vertex("-1");
        nf.setFinish(true);
        vs.setStart(true);
        int nfa_i = 2;
        Elem<Integer> idC = new Elem<>(1);
        Map<String,String> ids_p = new HashMap<>();
        List<Vertex> Fs = new ArrayList<>();
        for(String id : rules.keySet()){
            List<String> patterns = rules.get(id);
            for(String pattern : patterns){
                LinkedStack<Character> rpn = new LinkedStack<>();
                if(pattern.length() == 1) {
                    char c = pattern.charAt(0);
                    CNFA nfa = new CNFA();
                    Vertex v1 = new Vertex(nfa_i+"");
                    nfa_i++;
                    Vertex v2 = new Vertex(nfa_i+"");
                    nfa_i++;
                    Edge e = new Edge(v1,v2,c);
                    v1.setStart(true);
                    v2.setFinish(true);
                    nfa.setComboStart(v1);
                    nfa.setFinish(v2);
                    nfa.getFinish().setValue(id);
                    Fs.add(nfa.getFinish());
                    alpha.add(c);
                    idC.setV1(nfa_i);
                    Edge e2 = new Edge(vs,nfa.getStart(),(char)1);
                    //Edge e3 = new Edge(nfa.getFinish(),nf,(char)1);
                    continue;
                }
                else {
                    String p_i = new String(pattern.toCharArray());
                    p_i = p_i.replaceAll(G.getEmpty(),(char)1+"");
                    //System.out.println("Pattern: " +p_i);
                    parser.setTerminals(p_i.toCharArray());
                    p_i = Main.addConcat(p_i,parser);
                    parser.setTerminals(p_i.toCharArray());
                    //System.out.println("expr: "+p_i);
                    //System.out.println("Conc: "+ p_i);
                    //parser.setTerminals(p_i.toCharArray());
                    //System.out.println("PT:"+ Arrays.toString(parser.getTerminals()));
                    rpn = parser.GetInput(p_i);
                    //System.out.println("Stack:"+rpn);
                    //System.out.println("Parsed: "+rpn);
                }
                CNFA nfa = Main.buildNFA(rpn,parser,idC,Fs);
                alpha.addAll(nfa.getAlpha());
                nfa.getFinish().setValue(id);
                //System.out.println(nfa.getFinish().isFinish());
                Fs.addAll(nfa.getFinished());
                nfa_i = idC.getV1();
                nfa.getStart().setName(nfa_i+"");
                nfa_i++;
                idC.setV1(nfa_i);
                //System.out.println(nfa.getFinish());
                Edge e = new Edge(vs,nfa.getStart(),(char)1);
                //Edge ue = new Edge(nfa.getFinish(),nf,(char)1);
            }
        }
        CNFA comboNFA = new CNFA();
        comboNFA.setComboStart(vs);
        comboNFA.setFinished(Fs);
        System.out.println(comboNFA.getFinished());
//        System.out.println(comboNFA.getNodes());
//        System.out.println(comboNFA.getStart());
//        System.out.println(comboNFA.getFinished());
        alpha.remove((char)1);
        System.out.println("Alpha: "+alpha);
        comboNFA.setAlpha(alpha);
        System.out.println("States of NFA: "+comboNFA.getCountOfStates());
        return comboNFA;
    }
}
