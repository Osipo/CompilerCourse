package ru.osipov.labs.lab1.structures.automats;

import ru.osipov.labs.lab1.structures.graphs.*;
import ru.osipov.labs.lab1.structures.lists.LinkedQueue;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.utils.ColUtils;
import java.util.*;
import java.util.stream.Collectors;

public class DFA extends Graph {
    private Vertex start;
    private Vertex dead;
    private HashSet<Character> alpha;
    private HashSet<Vertex> finished;
    private HashMap<Pair<Vertex,Character>,Vertex> tranTable;

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
                if(U.contains(nfa.getFinish()))
                    finished.add(b);
                if(T.contains(nfa.getFinish()))
                    finished.add(a);
                Edge tran = new Edge(a,b,c);//Implies tran_function.
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
            }//there are only |Alpha| records.
            if(c == alpha.size()) {
                v.setDead(true);
                this.dead = v;
                break;
            }
        }

    }

    public void showTranTable(){
        System.out.println("Tran table of DFA "+tranTable.size());
        for(Pair<Vertex,Character> k : tranTable.keySet()){
            System.out.println("State: "+k.getV1()+" -> "+k.getV2()+" -> "+tranTable.get(k));
        }
    }

    //CREATE min-DFA based on DFA (minimize specified dfa)
    public DFA(DFA dfa) {
        this.alpha = dfa.getAlpha();
        Set<Vertex> F = dfa.getFinished();
        Set<Vertex> NF = dfa.getNodes().stream().filter(vertex -> !F.contains(vertex)).collect(Collectors.toSet());//Q - F.
        ArrayList<Set<Vertex>> P = minimize(NF, F, dfa.getNodes(), dfa.getTranTable());
        this.tranTable = new HashMap<Pair<Vertex, Character>, Vertex>();
        HashMap<Pair<Vertex,Character>,Vertex> oldTran = dfa.getTranTable();
        this.dead = null;
        this.start = null;
        this.finished = new HashSet<>();

        Elem<Integer> nstatescount = new Elem<>(0);
        HashMap<String,Vertex> mapped = new HashMap<>();
        for (Set<Vertex> sv : P) {//for each set -> make new state.
            for (Vertex v : sv) {//select a representer of the set
                    //System.out.println("Rep: "+v.getName());
                    Vertex n = new Vertex();
                    nstatescount.setV1(nstatescount.getV1() + 1);
                    makeRecord(oldTran,sv,v,n,nstatescount,mapped);
                    break;
            }
        }
        addNode(this.start);
    }

    private void makeRecord(HashMap<Pair<Vertex,Character>,Vertex> oldTran, Set<Vertex> group,Vertex s,Vertex n, Elem<Integer> count, HashMap<String,Vertex> mapped){
        n.setName("M"+count.getV1());
        //System.out.println("Rep: "+s);
        if(mapped.get(s.getName()) != null){
            n = mapped.get(s.getName());
            count.setV1(Integer.parseInt(n.getName().substring(1)));
        }
        if(group.stream().anyMatch(Vertex::isStart)){
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
        }
        List<Pair<Vertex,Character>> l = oldTran.keySet().stream().filter(x -> x.getV1().equals(s)).sorted(Comparator.comparing(Pair::getV2)).collect(Collectors.toList());
        for(Vertex s_i : group)
            mapped.put(s_i.getName(),n);
        for(Pair<Vertex,Character> k : l){
            Vertex t = oldTran.get(k);
            if(group.contains(t)) {
                Edge tran = new Edge(n, n, k.getV2());//loop to the same state if it has the same group.
                tranTable.put(new Pair<>(n,k.getV2()),n);//update new tran_table.
                mapped.put(t.getName(),n);
            }
            else if(!mapped.containsKey(t.getName())){//new group was not added.
                Vertex n2 = new Vertex();
                count.setV1(count.getV1() + 1);
                n2.setName("M"+count.getV1());
                Edge tran = new Edge(n,n2,k.getV2());
                tranTable.put(new Pair<>(n,k.getV2()),n2);
                mapped.put(t.getName(),n2);//mark group
            }
            else{//new group was added.
                Vertex prev = mapped.get(t.getName());
                Edge tran = new Edge(n,prev,k.getV2());
                tranTable.put(new Pair<>(n,k.getV2()),prev);
            }
        }
    }

    private ArrayList<Set<Vertex>> minimize(Set<Vertex> NF, Set<Vertex> F,List<Vertex> Q,HashMap<Pair<Vertex,Character>,Vertex> table){
        ArrayList<Set<Vertex>> P = new ArrayList<>(Q.size());
        P.add(F);//F
        P.add(NF);//Q - F (Q\F)
        HashMap<String,Integer> clz = new HashMap<>();//indicies of class which state is belonging.
        HashMap<Integer,Set<Vertex>> involved = new HashMap<>();//classes which have states with edges to splitter.
        for(Vertex q : F){
            clz.put(q.getName(),0);
        }
        for(Vertex q: NF){
            clz.put(q.getName(),1);
        }
        LinkedQueue<Pair<Set<Vertex>,Character>> queue = new LinkedQueue<>();
        for(Character c : alpha){
            queue.add(new Pair<Set<Vertex>,Character>(F,c));
            queue.add(new Pair<Set<Vertex>, Character>(NF,c));
        }
        while(!queue.isEmpty()){
            Pair<Set<Vertex>,Character> p = queue.front(); // pair <C, a>
            queue.dequeue();
            involved = new HashMap<>();

            //Compute involved..
            for(Vertex q : p.getV1()){// for q in C and r in Q such tran(r,a) in C
                List<Pair<Vertex,Character>> keys = table.keySet().stream().filter(x -> x.getV2() == p.getV2()).collect(Collectors.toList());
                for(Pair<Vertex,Character> k : keys) {
                    if (q.equals(table.get(k))) {//if q = tran(r,a)
                        Vertex r = k.getV1();
                        int i = clz.get(r.getName());
                        if (!involved.containsKey(i))
                            involved.put(i, new HashSet<Vertex>());
                        involved.get(i).add(r);
                        }
                    }
            }//Involved was computed.
            for(int i : involved.keySet()){
                Set<Vertex> P_i = P.get(i);  // ColUtils.<ArrayList<Set<Vertex>>,Set<Vertex>>getElemOfSet(P,i);
                if(involved.get(i).size() < P_i.size()){
                    Set<Vertex> P_j = new HashSet<>();
                    P.add(P_j);
                    int j = P.size() - 1;
                    for(Vertex r : involved.get(i)){
                        P_i.remove(r);
                        P_j.add(r);
                    }
                    if(P_j.size() > P_i.size()){
                        Collections.swap(P,i,j);
                        P_i = P.get(i);//ColUtils.<ArrayList<Set<Vertex>>, Set<Vertex>>getElemOfSet(P, i);
                        P_j = P.get(j);//ColUtils.<ArrayList<Set<Vertex>>, Set<Vertex>>getElemOfSet(P, j);
                    }
                    assert P_j != null;
                    for(Vertex v : P_j){
                        clz.put(v.getName(),j);
                    }
                    for(Character c : alpha){
                        Vertex vj = new Vertex();
                        vj.setName(""+j);
                        HashSet<Vertex> s = new HashSet<>();
                        s.add(vj);
                        queue.enqueue(new Pair<>(s,c));
                    }
                }
            }
        }
        System.out.println("P = "+P);
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
}