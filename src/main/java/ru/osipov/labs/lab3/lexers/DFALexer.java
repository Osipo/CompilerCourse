package ru.osipov.labs.lab3.lexers;

import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.structures.automats.NFA;
import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.graphs.Vertex;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DFALexer extends DFA implements ILexer {

    private LexerIO io;
    private Set<String> keywords;

    public DFALexer(DFA dfa, LexerIO io){
        super(dfa,true);
        this.deleteDeadState();
        this.io = io;
        this.keywords = new HashSet<>();
    }

    public DFALexer(NFA nfa, LexerIO io){
        super(nfa);
        this.deleteDeadState();
        this.io = io;
        this.keywords = new HashSet<>();
    }

    public DFALexer(CNFA nfa){
        super(nfa);
        this.deleteDeadState();
        this.keywords = new HashSet<>();
        System.out.println("DFA States: "+this.getNodes().size());
        System.out.println("Patterns (F): "+this.getFinished().size());
        System.out.println("Start: "+this.getStart());
        this.io = new LookAheadBufferedLexer();
    }


    public DFALexer(DFA dfa, int bsize){
        super(dfa,true);//set Minimization for lexer true.
        this.deleteDeadState();
        System.out.println("MinDFA States: "+this.getNodes().size());
        System.out.println("Patterns (F): "+this.getFinished().size());
        System.out.println("Start: "+this.getStart());
        this.io = new LookAheadBufferedLexer(bsize);
        this.keywords = new HashSet<>();
    }

    public DFALexer(DFA dfa){
        super(dfa,true);//set Minimization for lexer true.
        this.deleteDeadState();
        System.out.println("MinDFA States: "+this.getNodes().size());
        System.out.println("Patterns (F): "+this.getFinished().size());
        System.out.println("Start: "+this.getStart());
        this.io = new LookAheadBufferedLexer();
        this.keywords = new HashSet<>();
    }

    @Override
    public void setKeywords(Set<String> kws){
        this.keywords = kws;
    }

    @Override
    public Set<String> getKeywords(){
        return keywords;
    }

    @Override
    public Token recognize(InputStream f) throws IOException {
        char cur = (char)io.getch(f);
        while(cur == ' ' || cur == '\t' || cur == '\n' || cur == '\r') {
            if (cur == '\n') {
                io.setCol(0);
            }
            cur = (char) io.getch(f);
        }
        if(((int)cur) == 65535)
            return new Token("$","$",'t');
        Vertex s = this.start;
        LinkedStack<Vertex> states = new LinkedStack<>();
        states.push(s);
        StringBuilder sb = new StringBuilder();
        sb.append(cur);
        while(((int)cur) != 65535 && !s.isDead()){//while not EOF or not deadState.
            s = moveTo(s,cur);
            if(s == null || s.isDead()){
                //System.out.println("Lexeme at ("+io.getLine()+":"+io.getCol()+") :: "+sb.toString());
                String err = sb.toString();
                while(s == null || !s.isFinish()){
                    if(sb.length() == 0)
                        return new Token("Unrecognized ","Error at ("+io.getLine()+":"+io.getCol()+") :: Unrecognized token: "+err+"\n",'e');
                    cur = sb.charAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);
                    io.ungetch(cur);
                    s = states.top();
                    states.pop();
                }
                if(s.getValue().equals("id") && keywords.contains(sb.toString()))
                    return new Token(sb.toString(),sb.toString(), 't');
                return new Token(s.getValue(),sb.toString(),'t');
            }
            else {
                cur = (char)io.getch(f);
                sb.append(cur);
                states.push(s);
            }
        }
        if(sb.length() > 0){
            //System.out.println("Lexeme at ("+io.getLine()+":"+io.getCol()+") :: "+sb.toString());
            if((int)cur == 65535)
                sb.deleteCharAt(sb.length() - 1);//remove redundant read EOF ch.
            if(s.isFinish()) {
                if(s.getValue().equals("id") && keywords.contains(sb.toString()))
                    return new Token(sb.toString(),sb.toString(),'t');
                return new Token(s.getValue(), sb.toString(), 't');
            }
            else
                return new Token("Unrecognized","Error at ("+io.getLine()+":"+io.getCol()+") :: Unrecognized token: "+sb.toString()+"\n",'e');
        }
        else
            return new Token("$","$",'t');
    }

    private Vertex moveTo(Vertex v, char c){
        List<Pair<Vertex,Character>> k = tranTable.keySet().stream().filter(x -> x.getV1().equals(v) && x.getV2() == c).collect(Collectors.toList());
        return k.size() > 0 ? tranTable.get(k.get(0)) : null;
    }

    public Token generateError(String s1, String s2){
        return new Token("Unrecognized","Error at ("+io.getLine()+":"+io.getCol()+") :: Expected token: "+s1+"  but actual: "+s2+"\n",'e');
    }

    @Override
    public void reset() {
        io.setCol(0);
        io.setLine(1);
        io.clear();
    }
}
