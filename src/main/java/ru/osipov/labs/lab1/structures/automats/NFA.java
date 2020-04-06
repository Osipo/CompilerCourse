package ru.osipov.labs.lab1.structures.automats;

import ru.osipov.labs.lab1.structures.graphs.Edge;
import ru.osipov.labs.lab1.structures.graphs.Graph;
import ru.osipov.labs.lab1.structures.graphs.Vertex;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NFA extends Graph {
    private Vertex start;
    private Vertex finish;//when NFA is modeling, the Final states set is the set of the next possible states which contains final state.
    private HashSet<Character> alpha;

    public NFA(){}

    public void setStart(Vertex v){
        addNode(v);
        v.setStart(true);
        this.start = v;
        for(Vertex vd : nodes){
            if(vd.isFinish()) {
                this.finish = vd;
                break;
            }
        }
    }

    public void setAlpha(HashSet<Character> alpha) {
        this.alpha = alpha;
    }

    public HashSet<Character> getAlpha() {
        return alpha;
    }

    public Vertex getStart(){
        return start;
    }

    public Vertex getFinish(){
        return finish;
    }

    public int getCountOfStates(){
        return this.nodes.size();
    }

    public static Set<Vertex> e_closure(Set<Vertex> T){
        Set<Vertex> R = new HashSet<>(T);
        LinkedStack<Vertex> stack = new LinkedStack<>();
        for(Vertex v : T)
            stack.push(v);
        while(!stack.isEmpty()){
            Vertex t = stack.top();
            stack.pop();
            List<Vertex> l = t.getEdges().stream()
                    .filter(edge -> edge.getSource().equals(t) && edge.getTag() == (char)1)
                    .collect(Collectors.toList()).stream()
                    .map(Edge::getTarget).collect(Collectors.toList());

            for(Vertex u: l){
                if(!R.contains(u)){
                    R.add(u);
                    stack.push(u);
                }
            }
        }
        return R;
    }

    public static Set<Vertex> move(Set<Vertex> T,char a){
        Set<Vertex> R = new HashSet<>();
        for(Vertex v : T){
            List<Vertex> l = v.getEdges().stream()
                    .filter(edge -> edge.getSource().equals(v) && edge.getTag() == a)
                    .collect(Collectors.toList()).stream()
                    .map(Edge::getTarget).collect(Collectors.toList());
            R.addAll(l);
        }
        return R;
    }

    //Modeling NFA.
    //TODO: ADD TRACING
    public boolean Recognize(String i){
        HashSet<Vertex> S = new HashSet<>();
        S.add(this.start);
        Set<Vertex> s0 = e_closure(S);
        Set<Vertex> ps = s0;
        StringBuilder sb = new StringBuilder();
        int cs = 0;
        char t = 'e';
        while(cs < i.length()){
            t = i.charAt(cs);
            if(!alpha.contains(t))
                break;
            ps = s0;
            sb.append(t);
            System.out.println(ps+" -> "+t+" -> "+s0);
            s0 = e_closure(move(s0,t));
            if(s0.contains(this.finish)){
                System.out.println("Found lexem: "+sb.toString());
            }
            cs++;
        }
        return s0.contains(this.finish);
    }
}