package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.parsers.generators.CLRParserGenerator;
import ru.osipov.labs.lab3.parsers.generators.LR_0_Automaton;
import ru.osipov.labs.lab3.parsers.generators.SLRParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//Implements all LR parsers.
//Contains two LR parser generators.
//SLR parser or LR(0) parser in (SLRParserGenerator class)
//CLR parser or LR(1) parser in (CLRParserGenerator class)
public class LRParser extends Parser {

    private LR_0_Automaton table;

    public LRParser(Grammar G, ILexer lexer){
        this(G,lexer,LRAlgorithm.CLR);
    }

    public LRParser(Grammar G, ILexer lexer, LRAlgorithm alg) {
        super(G,lexer);
        try {
            if (alg == LRAlgorithm.SLR) {
                this.table = SLRParserGenerator.buildLRAutomaton(G);//LR(0) or SLR(1).
            } else {
                this.table = CLRParserGenerator.buildLRAutomaton(G);//LR(1)
            }
            System.out.println(table);
        }
        catch (Exception e){
            System.out.println("Cannot build");
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString(){
        return table.toString();
    }

    public LinkedTree<Token> parse(String fname){
        if(table == null)
            return null;
        int l,col = 0;
        try (FileInputStream f  = new FileInputStream(new File(fname).getAbsolutePath())){
            LinkedStack<LinkedNode<Token>> S = new LinkedStack<>();//symbols.
            LinkedStack<Integer> states = new LinkedStack<>();//states.
            isParsed = true;

            //READ first token
            Token tok = lexer.recognize(f);
            while(tok == null || tok.getName().equals("Unrecognized")){
                if(tok != null) {
                    System.out.println(tok);
                    isParsed = false;//TODO: MAY BE OPTIONAL.
                }
                tok = lexer.recognize(f);
            }
            l = tok.getLine();
            col = tok.getColumn();
            String t = tok.getName();

            int nidx = 1;//counter of elements (tree nodes)
            int cstate = 0;
            states.push(0);//push start state 0 to the STATES_STACK.
            String command = null;
            while(true) {
                cstate = states.top();
                Pair<Integer,String> k = new Pair<>(cstate,t);
                if(mode == ParserMode.DEBUG)
                    System.out.println(states+" "+S+" >>"+t);
                //command =  move_to s_state
                //      | r_header:size
                //      | acc
                //      | err
                command = table.getActionTable().get(k);
                if(command == null && empty == null){//if where are no any command and no empty.
                    isParsed = false;
                    break;
                }
                if(command == null){//if where is a empty symbol, try get command at [state, empty]
                    command = table.getActionTable().get(new Pair<Integer,String>(cstate,empty));
                    if(command != null){ //apply shift empty symbol if presence. (Rule like A -> e)
                        String j = command.substring(command.indexOf('_') + 1);
                        states.push(Integer.parseInt(j));
                        LinkedNode<Token> nc = new LinkedNode<>();
                        nc.setValue(new Token(empty,null,'t',l,col));
                        nidx++;
                        nc.setIdx(nidx);
                        S.push(nc);
                        continue;
                    }
                    isParsed = false;
                    break;
                }
                if(command.equals("err")){
                    isParsed = false;
                    break;
                }
                int argIdx = command.indexOf('_');
                argIdx = argIdx == -1 ? 1 : argIdx;//in case of ACC or ERR
                String act = command.substring(0,argIdx);
                if(act.charAt(0) == 's'){
                    String j = command.substring(argIdx + 1);
                    states.push(Integer.parseInt(j));
                    LinkedNode<Token> nc = new LinkedNode<>();
                    nc.setValue(tok);
                    nidx++;
                    nc.setIdx(nidx);
                    S.push(nc);
                    //get next token
                    tok = lexer.recognize(f);
                    while(tok == null || tok.getName().equals("Unrecognized")){
                        if(tok != null) {
                            isParsed = false;//TODO: MAY BE OPTIONAL
                            System.out.println(tok);
                        }
                        tok = lexer.recognize(f);
                    }
                    t = tok.getName();
                    l = tok.getLine();
                    col = tok.getColumn();
                }
                else if(act.charAt(0) == 'r'){
                    String args = command.substring(argIdx + 1);
                    String header = args.substring(0,args.indexOf(':'));
                    int sz = Integer.parseInt(args.substring(args.indexOf(':') + 1));
                    LinkedNode<Token> parent = new LinkedNode<>();
                    while(sz > 0){
                        LinkedNode<Token> c = S.top();
                        S.pop();
                        states.pop();
                        c.setParent(parent);
                        parent.getChildren().add(c);
                        sz--;
                    }
                    parent.setValue(new Token(header,header,'n',l,col));
                    nidx++;
                    parent.setIdx(nidx);
                    S.push(parent);
                    cstate = states.top();
                    states.push(table.getGotoTable().get(new Pair<Integer,String>(cstate,header)));
                }
                else if(act.charAt(0) == 'a'){
                    break;
                }
            }
            lexer.reset();
            if(isParsed){
                return new LinkedTree<Token>(S.top());
            }
            else
                return null;
        }
        catch (FileNotFoundException e){
            lexer.reset();
            System.out.println("File not found. Specify file to read");
            return null;
        } catch (IOException e) {
            lexer.reset();
            System.out.println("File is not available now.");
            return null;
        }
    }
}
