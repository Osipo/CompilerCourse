package ru.osipov.labs.lab1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.structures.automats.NFA;
import ru.osipov.labs.lab1.structures.graphs.Edge;
import ru.osipov.labs.lab1.structures.graphs.Vertex;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.utils.RegexRPNParser;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootConfiguration
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        RegexRPNParser parser = new RegexRPNParser();
        Scanner in = new Scanner(System.in);
        System.out.println("Input regex: ");
        String expr = in.nextLine();
        //String expr = "(ab|cd)*abb";
        //String expr2 = "(a|b)*[c-t]+";
        System.out.println("Expr: "+expr);
        String exprC = addConcat(expr,parser);
        //System.out.println("Test exrp2: "+addConcat(expr2,parser));
        System.out.println("Expr with concat: "+exprC);

        LinkedStack<Character> result = parser.GetInput(exprC);
        if(result == null){
            System.out.println("Illegal characters! Only in [A-Za-z0-9] allowed.");
            System.exit(0);
        }
        System.out.println("Stack: ");
        System.out.println(result);
        NFA nfa = buildNFA(result,parser);
        System.out.println("NFA: "+nfa.getNodes());
        System.out.println("Alphabet: "+nfa.getAlpha());
        DFA dfa = new DFA(nfa);
        System.out.println("DFA: "+dfa.getNodes());
        System.out.println("Start: "+dfa.getStart());
        System.out.println("Finish: "+dfa.getFinished());


        //dfa.showTranTable();
        DFA minDfa = new DFA(dfa);
        //minDfa.showTranTable();
        System.out.println("MinDFA: "+minDfa.getNodes());
        System.out.println("Start: "+minDfa.getStart());
        System.out.println("Dead: "+minDfa.getDead());
        System.out.println("Finish:"+minDfa.getFinished());

        System.out.println("Input str to recognize: ");
        String i = in.nextLine();

        System.out.println("Recognize nfa: "+nfa.Recognize(i));
        System.out.println("Recognize dfa: "+dfa.Recognize(i));
        System.out.println("Recognize minDfa: "+minDfa.Recognize(i));
        //System.out.println("DFA TABLE: ");
      //  dfa.showTranTable();
        System.out.println("MIN DFA: ");
        minDfa.showTranTable();
    }


    //Algorithm: Mac Naughton-Yamada-Tompson (Мак-Нотона, Ямады, Томпсона)
    public static NFA buildNFA(LinkedStack<Character> expr, RegexRPNParser parser){
        LinkedStack<NFA> result = new LinkedStack<>();
        HashSet<Character> alpha = new HashSet<>();
        for(Character tok : expr){
            if(parser.isUnaryOp(tok)){
                NFA g = result.top();
                result.pop();
                for(Vertex v: g.getNodes()){
                    v.setName("");
                    v.setFinish(false);
                }
                Vertex s = new Vertex();
                Vertex t = new Vertex();
                Edge iloop = new Edge(g.getFinish(), g.getStart(), (char) 1);
                g.getFinish().setFinish(false);
                g.getStart().setStart(false);
                Edge se = new Edge(s, g.getStart(), (char) 1);
                Edge fe = new Edge(g.getFinish(), t, (char) 1);
                if(tok == '*') {// '+' and '*' differ only with one edge.
                    Edge loop = new Edge(s, t, (char) 1);//for '*' add empty from start to finish
                }
                NFA R = new NFA();
                t.setFinish(true);
                R.setStart(s);
                result.push(R);
            }
            else if(parser.isOperator(tok)){
                NFA g2 = result.top();
                result.pop();
                NFA g1 = result.top();
                result.pop();
                for(Vertex v: g2.getNodes()){
                    v.setName("");v.setFinish(false);
                }
                for(Vertex v: g1.getNodes()){
                    v.setName("");v.setFinish(false);
                }
                if(tok == '^') {
                    Vertex inter = g1.getFinish();
                    inter.setFinish(false);
                    List<Edge> outE = g2.getStart().getEdges().stream().filter(edge -> edge.getSource().equals(g2.getStart())).collect(Collectors.toList());
                    for(Edge e: outE){
                        Edge ae = new Edge(inter,e.getTarget(),e.getTag());
                        g2.disconnectVertexByEdge(e,g2.getStart(),e.getTarget());
                    }
                    NFA FC = new NFA();
                    g2.getFinish().setFinish(true);
                    FC.setStart(g1.getStart());
                    result.push(FC);
                }
                else if(tok == '|'){
                    Vertex s = new Vertex();
                    Vertex t = new Vertex();
                    Vertex s1 = g1.getStart();
                    Vertex s2 = g2.getStart();
                    Vertex t1 = g1.getFinish();
                    Vertex t2 = g2.getFinish();
                    Edge s_s1 = new Edge(s,s1,(char)1);
                    Edge s_s2 = new Edge(s,s2,(char)1);
                    Edge t_t1 = new Edge(t1,t,(char)1);
                    Edge t_t2 = new Edge(t2,t,(char)1);
                    s.setStart(true);
                    s1.setStart(false);
                    s2.setStart(false);
                    t1.setFinish(false);
                    t2.setFinish(false);
                    t.setFinish(true);
                    NFA FU = new NFA();
                    FU.setStart(s);
                    result.push(FU);
                }
            }
            else{//token is not operator.
                Vertex v1 = new Vertex();
                Vertex v2 = new Vertex();
                v2.setFinish(true);
                Edge e = new Edge(v1,v2,tok);
                NFA F = new NFA();
                F.setStart(v1);
                alpha.add(tok);
                result.push(F);
            }
        }
        result.top().setAlpha(alpha);
        return result.top();
    }

    public static String addConcat(String s,RegexRPNParser parser){
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == '['){// replace class [A-Z] with expression (A|B|...|Z) and add '^' if needed.
                char t = s.charAt(i);
                if(i > 0){
                    result.append('^');
                }
                result.append('(');
                int j = i + 1;
                while(t != ']' && j < s.length()){
                    t = s.charAt(j);
                    if(j + 1 < s.length() && s.charAt(j) == '-'){
                        char a = s.charAt(j - 1);
                        char b = s.charAt(j + 1);
                        if(a > b){
                            char temp = a;
                            a = b;
                            b = temp;
                        }
                        while(a != b){
                            a++;
                            result.append(a).append("|");
                        }
                        CharSequence subor = result.subSequence(0,result.length() - 1);
                        result = new StringBuilder().append(subor);
                        j = j + 2;
                        continue;
                    }
                    String or =  (j + 1 == s.length() || s.charAt(j + 1) == ']') ? s.charAt(j)+"" : s.charAt(j)+"|";
                    result.append(s.charAt(j) == ']' ? ")" : or);
                    j++;
                }
                if(j < s.length() && s.charAt(j) == ']')
                    result.append('^');
                if(j + 1 == s.length()) {
                    result.append(s.charAt(j));
                    return result.toString();
                }
                i = j;
                continue;
            }
            result.append(s.charAt(i));
            if(parser.isTerminal(s.charAt(i)) && i + 1 < s.length() && (parser.isTerminal(s.charAt(i + 1)) || s.charAt(i + 1) == '(' ) ){
                result.append('^');
            }
            if((s.charAt(i) == ')' || s.charAt(i) == '*' || s.charAt(i) == '+' ) && i + 1 < s.length() && (parser.isTerminal(s.charAt(i + 1)) || s.charAt(i + 1) == '(') ){
                result.append('^');
            }
        }
        return result.toString();
    }

    @Override
    public void run(String... args) throws Exception {
        main(args);
    }
}