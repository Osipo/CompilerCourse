package ru.osipov.labs.lab3.lexers;

import ru.osipov.labs.lab1.structures.automats.CNFA;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.structures.automats.NFA;
import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.graphs.Vertex;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
public class DFALexer extends DFA implements ILexer, ILexerConfiguration {

    private LexerIO io;//reader of stream of chars (from file or str or etc.)
    private Token prevTok;// lexer previous recognized Token.
    private char[] ignore;// symbols that are ignored at InputStream

    /* these fields are allusion for GrammarMetaInfo type */
    /* The reason is why GrammarMetaInfo is not used here
       is that we need fully independent Lexer without coupling to specific Grammar.
     */
    private String commentStart;
    private String mlcStart;
    private String mlcEnd;
    private String id;
    private Set<String> keywords;
    private Set<String> operands;
    private Map<String,String> aliases;
    private Map<String, List<String>> separators;// symbols that are part of regex but not a part of lexem

    /*TODO: Make builder instead of .ctors */
    public DFALexer(DFA dfa, LexerIO io){
        super(dfa,true);
        this.deleteDeadState();
        this.io = io;
        this.keywords = new HashSet<>();
        this.operands = new HashSet<>();
        this.aliases = new HashMap<>();
        this.commentStart = null;
        this.mlcStart = null;
        this.mlcEnd = null;
        this.id = null;
        this.prevTok = null;
        this.ignore = null;
        this.separators = null;
    }

    public DFALexer(NFA nfa, LexerIO io){
        super(nfa);
        this.deleteDeadState();
        this.io = io;
        this.keywords = new HashSet<>();
        this.operands = new HashSet<>();
        this.aliases = new HashMap<>();
        this.prevTok = null;
        this.commentStart = null;
        this.mlcStart = null;
        this.mlcEnd = null;
        this.id = null;
        this.ignore = null;
        this.separators = null;
    }

    public DFALexer(CNFA nfa){
        super(nfa);
        this.deleteDeadState();
        this.keywords = new HashSet<>();
        System.out.println("DFA States: "+this.getNodes().size());
        System.out.println("Patterns (F): "+this.getFinished().size());
        System.out.println("Start: "+this.getStart());
        this.io = new LookAheadBufferedLexer();
        this.keywords = new HashSet<>();
        this.operands = new HashSet<>();
        this.aliases = new HashMap<>();
        this.prevTok = null;
        this.commentStart = null;
        this.mlcStart = null;
        this.mlcEnd = null;
        this.id = null;
        this.ignore = null;
        this.separators = null;
    }


    public DFALexer(DFA dfa, int bsize){
        super(dfa,true);//set Minimization for lexer true.
        this.deleteDeadState();
        System.out.println("MinDFA States: "+this.getNodes().size());
        System.out.println("Patterns (F): "+this.getFinished().size());
        System.out.println("Start: "+this.getStart());
        this.io = new LookAheadBufferedLexer(bsize);
        this.keywords = new HashSet<>();
        this.operands = new HashSet<>();
        this.aliases = new HashMap<>();
        this.prevTok = null;
        this.commentStart = null;
        this.mlcStart = null;
        this.mlcEnd = null;
        this.id = null;
        this.ignore = null;
        this.separators = null;
    }

    public DFALexer(DFA dfa){
        super(dfa,true);//set Minimization for lexer true.
        this.deleteDeadState();
        System.out.println("MinDFA States: "+this.getNodes().size());
        System.out.println("Patterns (F): "+this.getFinished().size());
        System.out.println("Start: "+this.getStart());
        this.io = new LookAheadBufferedLexer();
        this.keywords = new HashSet<>();
        this.operands = new HashSet<>();
        this.aliases = new HashMap<>();
        this.prevTok = null;
        this.commentStart = null;
        this.mlcStart = null;
        this.mlcEnd = null;
        this.id = null;
        this.ignore = null;
        this.separators = null;
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
    public void setSeparators(Map<String, List<String>> separators) {
        this.separators = separators;
    }

    @Override
    public void setOperands(Set<String> operands) {
        this.operands = operands;
    }

    @Override
    public Set<String> getOperands() {
        return operands;
    }

    @Override
    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public Map<String, String> getAliases() {
        return aliases;
    }

    @Override
    public void setCommentLine(String s){
        this.commentStart = s;
    }

    @Override
    public void setMlCommentStart(String s){
        this.mlcStart = s;
    }

    @Override
    public void setMlCommentEnd(String s){
        this.mlcEnd = s;
    }

    @Override
    public void setIdName(String s){
        this.id = s;
    }

    public String getIdName(){
        return this.id;
    }

    @Override
    public Token recognize(InputStream f) throws IOException {
        char cur = (char)io.getch(f);


        //If IGNORE IS NOT SET THEN DELETE space symbols at Start of Token
        if(ignore == null || ignore.length == 0) {
            //System.out.println("Ignore symbols are null");
            while (cur == ' ' || cur == '\t' || cur == '\n' || cur == '\r') {
                if (cur == '\n') {
                    io.setCol(0);
                }
                cur = (char) io.getch(f);
            }
        }
        /* SKIP separators Symbols at Start of Token */
        else {
            while(Arrays.binarySearch(ignore, cur) >= 0 && ((int)cur) != 65535){
                if(cur == '\n')
                    io.setCol(0);
                cur = (char) io.getch(f);
            }
        }
        if(((int)cur) == 65535)
            return new Token("$","$",'t',io.getLine(),io.getCol());
        Vertex s = this.start;// s == state.
        LinkedStack<Vertex> states = new LinkedStack<>();
        states.push(s);
        StringBuilder sb = new StringBuilder();
        sb.append(cur);
        int pl = 0;
        while(((int)cur) != 65535 && !s.isDead()){//while not EOF or not deadState.
            s = moveTo(s,cur);
            if(s == null || s.isDead()){
                //System.out.println("Lexeme at ("+io.getLine()+":"+io.getCol()+") :: "+sb.toString());
                String err = sb.toString();
                while(s == null || !s.isFinish()){
                    if(sb.length() == 0) {
                        prevTok =  new Token("Unrecognized", "Lexical error at (" + io.getLine() + ":" + io.getCol() + ") :: Unrecognized token: " + err + "\n", 'e',io.getLine(),io.getCol());
                        while(pl > 0){
                            pl--;
                            io.getch(f);//skip unrecognized word.
                        }
                        return prevTok;
                    }
                    cur = sb.charAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);
                    //System.out.println(sb);
                    io.ungetch(cur);//get back one symbol
                    pl++;//
                    s = states.top();
                    states.pop();
                }
                /* REGION_BEGIN_COMMENTS */
                if(s.getValue().equals(this.commentStart)){//commentLine start.
                    while(cur != '\n' && ((int)cur) != 65535){//check also EOF symbol
                        cur = (char)io.getch(f);
                    }
                    return null;//return null for comments.
                }
                else if(s.getValue().equals(this.mlcStart)) {
                    int mls = 0;
                    int mle = mlcEnd.length();
                    boolean isp = false;
                    while (cur != 65535) {//cur != EOF
                        cur = (char) io.getch(f);
                        while (mls < mle && mlcEnd.charAt(mls) == cur) {
                            cur = (char) io.getch(f);
                            isp = true;
                            mls++;
                        }
                        if (mls == mle) {
                            return null;//mlComment found. return null
                        } else {
                            mls = 0;
                            if (isp)
                                io.ungetch(cur);
                            isp = false;
                        }
                    }
                    return new Token("$", "$", 't', io.getLine(), io.getCol());
                } /* REGION_END_COMMENTS */
                /* KEYWORD (comment first condition at predicate if needed)*/
                if((s.getValue().equals(this.id) || this.id == null) && keywords.size() > 0 && keywords.contains(sb.toString())) {
                    prevTok = makeToken(sb.toString(), sb.toString());//new Token(sb.toString(), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
                else if(prevTok == null || (operands.size() > 0 && operands.contains(prevTok.getName())) ){
                    prevTok =  makeToken(s.getValue(), sb.toString());//new Token(s.getValue(), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
                else if(prevTok != null && operands.size() > 0){
                    prevTok = makeToken(aliases.getOrDefault(s.getValue(), s.getValue()), sb.toString());//new Token(aliases.getOrDefault(s.getValue(),s.getValue()), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
                else {
                    prevTok =  makeToken(s.getValue(), sb.toString());//new Token(s.getValue(), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
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
                /* REGION_BEGIN_COMMENTS */
                if(s.getValue().equals(this.commentStart)){//commentLine start.
                    while(cur != '\n' && ((int)cur) != 65535){//check also EOF symbol
                        cur = (char)io.getch(f);
                    }
                    return null;//return null for comments.
                }
                else if(s.getValue().equals(this.mlcStart)){
                    int mls = 0; int mle = mlcEnd.length();
                    boolean isp = false;
                    while(cur != 65535){//cur != EOF
                        cur = (char)io.getch(f);
                        while(mlcEnd.charAt(mls) == cur && mls < mle){
                            cur = (char)io.getch(f);
                            isp = true;
                            mls++;
                        }
                        if(mls == mle){
                            return null;//mlComment found. return null
                        }
                        else {
                            mls = 0;
                            if(isp)
                                io.ungetch(cur);
                            isp = false;
                        }
                    }
                    return new Token("$","$",'t',io.getLine(),io.getCol());
                }/* REGION_END_COMMENTS */
                /* KEYWORD (comment first condition at predicate if needed)*/
                if((s.getValue().equals(this.id) || this.id == null) && keywords.size() > 0 && keywords.contains(sb.toString())) {
                    prevTok = makeToken(sb.toString(), sb.toString());//new Token(sb.toString(), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
                else if(prevTok == null || (operands.size() > 0 && operands.contains(prevTok.getName())) ){
                    prevTok =  makeToken(s.getValue(), sb.toString());//new Token(s.getValue(), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
                else if(prevTok != null && operands.size() > 0){
                    prevTok = makeToken(aliases.getOrDefault(s.getValue(),s.getValue()), sb.toString());//new Token(aliases.getOrDefault(s.getValue(),s.getValue()), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
                else {
                    prevTok =  makeToken(s.getValue(), sb.toString());//new Token(s.getValue(), sb.toString(), 't',io.getLine(),io.getCol());
                    return prevTok;
                }
            }
            else {
                prevTok =  new Token("Unrecognized", "Error at (" + io.getLine() + ":" + io.getCol() + ") :: Unrecognized token: " + sb.toString() + "\n", 'e',io.getLine(),io.getCol());
                return prevTok;
            }
        }
        else {
            prevTok =  new Token("$", "$", 't',io.getLine(),io.getCol());
            return prevTok;
        }
    }

    /* construct new Token WITH scanning separators */
    protected Token makeToken(String name, String val){
        if(this.separators == null)
            return new Token(name, val, 't', io.getLine(), io.getCol());
        else if(this.separators.getOrDefault(name, null) == null) {
            return new Token(name, val, 't', io.getLine(), io.getCol());
        }
        List<Character> symbols = this.separators.get(name).stream().map(x -> x.charAt(0)).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();

        /* Do not include symbols from separators */
        for(int i = 0; i < val.length(); i++){
            if(symbols.contains(val.charAt(i)))
                continue;
            sb.append(val.charAt(i));
        }
        return new Token(name, sb.toString(), 't', io.getLine(), io.getCol());
    }

    //Try move on from current character c
    //IF NOT => Try move on from any character (search edge with any label (char) 0)
    //IF NOT => return null.
    private Vertex moveTo(Vertex v, char c){
        List<Pair<Vertex,Character>> k = tranTable.keySet().stream().filter(x -> x.getV1().equals(v) && x.getV2() == c ).collect(Collectors.toList());
        List<Pair<Vertex,Character>> k2 = tranTable.keySet().stream().filter(x -> x.getV1().equals(v) && x.getV2() == (char)0).collect(Collectors.toList());
        return k.size() > 0 ? tranTable.get(k.get(0)) : k2.size() > 0 ? tranTable.get(k2.get(0)) : null;
    }

    public Token generateError(String s1, String s2){
        return new Token("Unrecognized","Error at ("+io.getLine()+":"+io.getCol()+") :: Expected token: "+s1+"  but actual: "+s2+"\n",'e',io.getLine(),io.getCol());
    }

    @Override
    public void reset() {
        io.setCol(0);
        io.setLine(1);
        io.clear();
        this.prevTok = null;
    }

    @Override
    public void setIgnoreSymbols(char[] symbols) {
        //COPY ARRAY symbols to ignore (NOT BY REFERENCE!)
        if(symbols == null) {
            this.ignore = null;
            System.out.println("default ignore symbols are set [nl,tab,r,space]");
            return;
        }
        this.ignore = new char[symbols.length];
        System.arraycopy(symbols, 0, this.ignore, 0, ignore.length);
        Arrays.sort(this.ignore);
        //System.out.println("Ignore symbols: "+ Arrays.toString(this.ignore));
    }
}
