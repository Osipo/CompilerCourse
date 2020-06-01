package ru.osipov.labs.lab1.structures.automats;

import ru.osipov.labs.lab1.structures.graphs.*;
import ru.osipov.labs.lab1.structures.lists.LinkedQueue;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.utils.ColUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DFA extends Graph {
    protected Vertex start;
    protected Vertex dead;
    protected HashSet<Character> alpha;
    protected HashSet<Vertex> finished;
    protected HashMap<Pair<Vertex,Character>,Vertex> tranTable;

    public DFA(HashMap<Pair<Vertex,Character>,Vertex> tranTable, Vertex start, HashSet<Vertex> fihished){
        this.start = start;
        this.finished = fihished;
        this.tranTable = tranTable;
        this.dead = null;
        this.alpha = new HashSet<>();
        for(Pair<Vertex,Character> p : tranTable.keySet()){
            alpha.add(p.getV2());
        }
    }

    public DFA(NFA nfa){
        this.finished = new HashSet<>();
        this.tranTable = new HashMap<>();
        LinkedStack<Set<Vertex>> ST = new LinkedStack<>();
        HashSet<Vertex> s0 = new HashSet<>();
        s0.add(nfa.getStart());

        ST.push(NFA.e_closure(s0));//e-closure(s0) where s0 - start state.
        this.start = new Vertex();//label for e-closure(s0)
        this.start.setStart(true);
        this.alpha = nfa.getAlpha();
        this.alpha.remove((char)1);
        this.start.setName("1");
        Vertex a = this.start;
        Vertex b = null;
        HashMap<Set<Vertex>,Vertex> mapped = new HashMap<>();//1 iteration map s0 to a single state.
        mapped.put(ST.top(),a);
        int qn = 1;
        while(!ST.isEmpty()){
            Set<Vertex> T = ST.top();
            ST.pop();
            a = mapped.get(T);//get mapped state. (after 1 iteration)
            for(char c : alpha){
                Set<Vertex> U = NFA.e_closure(NFA.move(T,c));
                if(!mapped.containsKey(U)){
                    ST.push(U);
                    qn++;
                    b = new Vertex();
                    b.setName(qn+"");
                    mapped.put(U,b);//add new unmapped state.
                }
                else
                    b = mapped.get(U);//get mapped state.
                if(U.contains(nfa.getFinish())) {
                    b.setValue(nfa.getFinish().getValue());
                    finished.add(b);
                }
                if(T.contains(nfa.getFinish())) {
                    a.setValue(nfa.getFinish().getValue());
                    finished.add(a);
                }
                Edge tran = new Edge(a,b,c);//Implies tran_function.
                this.edges.add(tran);
                tranTable.put(new Pair<>(a,c),b);
            }
        }
        addNode(this.start);//build graph
        for(Vertex vv: finished){
            vv.setFinish(true);
        }
        //initiate dead_state.
        for(Vertex v : nodes){
            int c = 0;
            List<Pair<Vertex,Character>> k = tranTable.keySet().stream().filter(x -> x.getV1().equals(v)).collect(Collectors.toList());
            for(Pair<Vertex,Character> kk : k){
                if(tranTable.get(kk).equals(v))
                    c++;
            }//if any transition leads to the same vertex.
            if(c == alpha.size()) {
                v.setDead(true);
                this.dead = v;
                break;
            }
        }

    }

    public DFA(CNFA nfa){
        this.finished = new HashSet<>();
        this.tranTable = new HashMap<>();
        LinkedStack<Set<Vertex>> ST = new LinkedStack<>();
        HashSet<Vertex> s0 = new HashSet<>();
        s0.add(nfa.getStart());

        ST.push(NFA.e_closure(s0));//e-closure(s0) where s0 - start state.
        this.start = new Vertex();//label for e-closure(s0)
        this.start.setStart(true);
        this.alpha = nfa.getAlpha();
        this.start.setName("1");
        Vertex a = this.start;
        Vertex b = null;
        HashMap<Set<Vertex>,Vertex> mapped = new HashMap<>();//1 iteration map s0 to a single state.
        mapped.put(ST.top(),a);
        int qn = 1;
        while(!ST.isEmpty()){
            Set<Vertex> T = ST.top();
            ST.pop();
            a = mapped.get(T);//get mapped state. (after 1 iteration)
            for(char c : alpha){
                Set<Vertex> U = NFA.e_closure(NFA.move(T,c));
                if(!mapped.containsKey(U)){
                    ST.push(U);
                    qn++;
                    b = new Vertex();
                    b.setName(qn+"");
                    mapped.put(U,b);//add new unmapped state.
                }
                else
                    b = mapped.get(U);//get mapped state.
                Set<Vertex> FF = new HashSet<>(U);
                FF.retainAll(nfa.getFinished());
                if(FF.size() > 0) {//U.contains(nfa.getFinish())
                    b.setValue(FF.stream().findFirst().get().getValue());
                    finished.add(b);
                }
                FF = new HashSet<>(T);
                FF.retainAll(nfa.getFinished());
                if(FF.size() > 0) {
                    a.setValue(FF.stream().findFirst().get().getValue());
                    finished.add(a);
                }
                Edge tran = new Edge(a,b,c);//Implies tran_function.
                this.edges.add(tran);
                tranTable.put(new Pair<>(a,c),b);
            }
        }
        addNode(this.start);//build graph
        for(Vertex vv: finished){
            vv.setFinish(true);
        }
        //initiate dead_state.
        for(Vertex v : nodes){
            int c = 0;
            List<Pair<Vertex,Character>> k = tranTable.keySet().stream().filter(x -> x.getV1().equals(v)).collect(Collectors.toList());
            for(Pair<Vertex,Character> kk : k){
                if(tranTable.get(kk).equals(v))
                    c++;
            }//if any transition leads to the same vertex.
            if(c == alpha.size()) {
                v.setDead(true);
                this.dead = v;
                break;
            }
        }
        System.out.println("States of DFA: "+this.nodes.size());
        System.out.println("Finished: "+this.finished.size());
        if(dead != null)
            System.out.println("Dead: "+dead);
    }

    public void showTranTable(){
        System.out.println("Tran table of DFA "+tranTable.size());
        for(Pair<Vertex,Character> k : tranTable.keySet()){
            System.out.println("State: "+k.getV1()+" -> "+k.getV2()+" -> "+tranTable.get(k));
        }
    }

    //CREATE min-DFA based on DFA (minimize specified dfa)
    public DFA(DFA dfa,boolean isLexer) {
        this.alpha = dfa.getAlpha();
        Set<Vertex> F = dfa.getFinished();
        Set<Vertex> NF = dfa.getNodes().stream().filter(vertex -> !F.contains(vertex)).collect(Collectors.toSet());//Q - F.
        ArrayList<Set<Vertex>> P = minimize(NF, F, dfa.getNodes(), dfa.getTranTable(),isLexer);
        this.tranTable = new HashMap<Pair<Vertex, Character>, Vertex>();
        HashMap<Pair<Vertex,Character>,Vertex> oldTran = dfa.getTranTable();
        this.dead = null;
        this.start = null;
        this.finished = new HashSet<>();

        Elem<Integer> nstatescount = new Elem<>(0);
        HashMap<String,Vertex> mapped = new HashMap<>();
        HashMap<String,Set<Vertex>> representers = new HashMap<>();
        for (Set<Vertex> sv : P) {//for each set -> make new state.
            for (Vertex v : sv) {//select a representer of the set
                Vertex n = new Vertex();
                nstatescount.setV1(nstatescount.getV1() + 1);
                makeRecord(P,oldTran,sv,v,n,nstatescount,mapped);
                break;
            }
        }
        addNode(this.start);
    }

    //CREATE min-DFA based on DFA (minimize specified dfa)
    public DFA(DFA dfa) {
        this(dfa,false);
    }



    //MAKERECORD__ERR
    private void makeRecord(ArrayList<Set<Vertex>> P,HashMap<Pair<Vertex,Character>,Vertex> oldTran, Set<Vertex> group,Vertex s,Vertex n, Elem<Integer> count, HashMap<String,Vertex> mapped){
        n.setName("M"+count.getV1());
        n.setValue(s.getValue());
        if(mapped.get(s.getName()) != null){
            n = mapped.get(s.getName());
        }
        if(group.stream().anyMatch(Vertex::isStart)){
            System.out.println(n);
            this.start = n;
            n.setStart(true);
        }
        if(group.stream().anyMatch(Vertex::isDead)){
            this.dead = n;
            n.setDead(true);
        }
        if(group.stream().anyMatch(Vertex::isFinish)){
            this.finished.add(n);
            n.setFinish(true);
            n.setValue(s.getValue());
        }
        List<Pair<Vertex,Character>> l = oldTran.keySet().stream().filter(x -> x.getV1().equals(s)).sorted(Comparator.comparing(Pair::getV2)).collect(Collectors.toList());
        for(Vertex s_i : group)
            mapped.put(s_i.getName(),n);
        for(Pair<Vertex,Character> k : l){
            Vertex t = oldTran.get(k);
            if(group.contains(t)) {
                Edge tran = new Edge(n, n, k.getV2());//loop to the same state if it has the same group.
                this.edges.add(tran);
                tranTable.put(new Pair<>(n,k.getV2()),n);//update new tran_table.
                mapped.put(t.getName(),n);
            }
            else if(!mapped.containsKey(t.getName())){//new group was not added.
                Vertex n2 = new Vertex();
                count.setV1(count.getV1() + 1);
                n2.setName("M"+count.getV1());
                Edge tran = new Edge(n,n2,k.getV2());
                this.edges.add(tran);
                tranTable.put(new Pair<>(n,k.getV2()),n2);
                mapped.put(t.getName(),n2);//mark group
                //another group must be fully labeled!
                P.stream().filter(x -> x.contains(t)).forEach(
                        x ->{
                            for(Vertex g2_v : x)
                                mapped.put(g2_v.getName(),n2);
                        }
                );
            }
            else{//new group was added.
                Vertex prev = mapped.get(t.getName());
                Edge tran = new Edge(n,prev,k.getV2());
                this.edges.add(tran);
                tranTable.put(new Pair<>(n,k.getV2()),prev);
            }
        }
    }

    //isLexer :: ifTrue => build initial partition with patterns, dead state and others. ({p1},{p2}...{pn}{dead}{NF states})
    private ArrayList<Set<Vertex>> minimize(Set<Vertex> NF, Set<Vertex> F,List<Vertex> Q,HashMap<Pair<Vertex,Character>,Vertex> table,boolean isLexer){
        ArrayList<Set<Vertex>> P = new ArrayList<>(Q.size());
        HashMap<String,Integer> clz = new HashMap<>();//indicies of class which state is belonging.
        HashMap<Integer,Set<Vertex>> involved = new HashMap<>();//classes which have states with edges to splitter.
        LinkedQueue<Pair<Set<Vertex>,Character>> queue = new LinkedQueue<>();
        ArrayList<Set<Vertex>> W = new ArrayList<>();
        if(isLexer) {
            List<Vertex> l = ColUtils.fromSet(F);
            Vertex dead = null;
            for(int i = 0; i < l.size();i++){
                if(l.get(i).isDead()){
                    dead = l.get(i);
                    break;
                }
            }
            if(dead == null){
                for(Vertex v : NF){
                    if(v.isDead()){
                        dead = v;
                        break;
                    }
                }
                NF.remove(dead);
            }
            l.remove(dead);
            LinkedStack<String> ids = new LinkedStack<>();
            HashMap<String,Set<Vertex>> ig = new HashMap<>();
            for(int i = 0; i < l.size();i++){
                String gname = l.get(i).getValue();
                if(!ids.contains(gname)){
                    ids.push(gname);
                    ig.put(gname,new HashSet<>());
                }
            }
            for(int i = 0; i < l.size();i++){
                Set<Vertex> group = ig.get(l.get(i).getValue());
                group.add(l.get(i));
            }
            P.add(NF);
            for(String k : ig.keySet()){
                Set<Vertex> group = ig.get(k);
                P.add(group);
            }
            if(dead != null) {
                Set<Vertex> Dead = new HashSet<>();
                Dead.add(dead);
                P.add(Dead);
            }
            //System.out.println("IP = "+P);
            W.addAll(P);
        }
        else {
            P.add(F);//F
            P.add(NF);//Q - F (Q\F)
            W.add(F);
            W.add(NF);
        }
        while(W.size() > 0){
            Set<Vertex> C = W.get(0);
            W.remove(C);
            for(Character c : alpha){
                List<Pair<Vertex,Character>> keys = table.keySet().stream().filter(x -> x.getV2() == c).collect(Collectors.toList());
                Set<Vertex> X = new HashSet<>();
                for(Pair<Vertex,Character> k : keys) {
                    if(C.contains(table.get(k))){
                        X.add(k.getV1());
                    }
                }
                int k = 0;
                while(k < P.size()) {
                    Set<Vertex> Y = P.get(k);
                    Set<Vertex> Y_i = new HashSet<>(Y);
                    Set<Vertex> X_i = new HashSet<>(X);
                    Y_i.removeAll(X);//Y - X.
                    X_i.retainAll(Y);// X INTERSECT Y
                    if (X_i.size() > 0 && Y_i.size() > 0){
                        P.remove(Y);
                        P.add(X_i);//replaced current
                        P.add(Y_i);
                        if(W.contains(Y)){
                            W.remove(Y);
                            W.add(X_i);
                            W.add(Y_i);
                        }
                        else{
                            if(X_i.size() <= Y_i.size()){
                                W.add(X_i);
                            }
                            else
                                W.add(Y_i);
                        }
                    }
                    k++;
                }
            }
        }
        //System.out.println("P = "+P);
        return P;
    }


    public void setAlpha(HashSet<Character> alpha) {
        this.alpha = alpha;
    }

    public HashSet<Character> getAlpha() {
        return alpha;
    }

    public Vertex getStart() {
        return start;
    }

    public Vertex getDead(){
        return dead;
    }

    public HashMap<Pair<Vertex,Character>,Vertex> getTranTable(){
        return tranTable;
    }

    public Set<Vertex> getFinished() {
        return finished;
    }

    public int getCountOfStates(){
        return this.nodes.size();
    }

    //TODO: ADD TRACING
    public boolean Recognize(String i){
        Vertex s = this.start;
        int cs = 0;
        StringBuilder sb = new StringBuilder();
        while(cs < i.length()){
            char finalC = i.charAt(cs);
            if(!alpha.contains(finalC))
                break;
            sb.append(finalC);
            Vertex finanS = s;
            Pair<Vertex,Character> k = tranTable.keySet().stream().filter(x -> x.getV1().equals(finanS) && x.getV2() == finalC).collect(Collectors.toList()).get(0);
            s = tranTable.get(k);//1 -> a -> 2.
            System.out.println("State: "+finanS+" -> "+finalC+" -> "+s);//tracking.
            if(s.isFinish()){
                System.out.println("Found lexem: "+sb.toString());
            }
            else if(s.isDead()){
                System.out.println("Syntax error. Cannot find valid lexem for regex in "+i.substring(cs));
                break;
            }
            cs++;
        }
        return s.isFinish();
    }

    //Delete dead state from DFA.
    public void deleteDeadState(){
        if(dead != null){
            List<Edge> conns = new ArrayList<>(dead.getEdges());
            this.nodes.remove(dead);//remove from nodes.
            for(Edge e : conns){
                Pair<Vertex,Character> p1 = new Pair<>(e.getSource(),e.getTag());
                Pair<Vertex,Character> p2 = new Pair<>(e.getTarget(),e.getTag());
                if(tranTable.get(p1) != null)//remove from tranTable.
                    tranTable.remove(p1);
                if(tranTable.get(p2) != null)
                    tranTable.remove(p2);
                this.edges.remove(e);//remove from edges.
                e.disconnectNodes();
            }
            this.dead = null;
        }
    }

    //Add nodes to the graph. All edges has already been added before building DFA.
    //(On step constructing DFA from NFA) or
    //(On step minimization DFA)
    //param: Vertex v is useless (used only to override method)
    @Override
    public void addNode(Vertex v){
        for(Pair<Vertex,Character> k : tranTable.keySet()){
            Vertex dest = tranTable.get(k);
            if(!nodes.contains(dest))
                this.nodes.add(dest);
            if(!nodes.contains(k.getV1()))
                this.nodes.add(k.getV1());
        }
    }
}