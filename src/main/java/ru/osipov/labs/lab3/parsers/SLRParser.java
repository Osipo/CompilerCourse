package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.parsers.generators.LR_0_Automaton;
import ru.osipov.labs.lab3.parsers.generators.SLRParserGenerator;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.LinkedTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//SLR parser or LR(0) parser.
public class SLRParser extends Parser {

    private LR_0_Automaton table;
    public SLRParser(Grammar G, ILexer lexer){
        super(G,lexer);
        this.table = SLRParserGenerator.buildLRAutomaton(G);
    }

    @Override
    public String toString(){
        return table.toString();
    }

    public LinkedTree<Token> parse(String fname){
        try (FileInputStream f  = new FileInputStream(new File(fname).getAbsolutePath())){
            LinkedStack<LinkedNode<Token>> S = new LinkedStack<>();//symbols.
            LinkedStack<Integer> states = new LinkedStack<>();//states.
            //READ first token
            Token tok = lexer.recognize(f);
            while(tok == null){
                tok = lexer.recognize(f);
            }
            String t = tok.getName();

            isParsed = true;
            int nidx = 1;//counter of elements (tree nodes)
            int cstate = 0;
            states.push(0);//push start state 0 to the STATES_STACK.
            String command = null;
            while(true) {
                cstate = states.top();
                Pair<Integer,String> k = new Pair<>(cstate,t);

                //command =  s_state
                //      | r_header:size
                //      | acc
                //      | err
                command = table.getActionTable().get(k);
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
                    while(tok == null){
                        tok = lexer.recognize(f);
                    }
                    t = tok.getName();
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
                    parent.setValue(new Token(header,header,'n'));
                    nidx++;
                    parent.setIdx(nidx);
                    S.push(parent);
                    cstate = states.top();
                    states.push(table.getGotoTable().get(new Pair<Integer,String>(cstate,header)));
                }
                else if(act.charAt(0) == 'a'){
                    break;
                }
                else if(act.charAt(0) == 'e'){
                    isParsed = false;
                    break;
                }
            }
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
