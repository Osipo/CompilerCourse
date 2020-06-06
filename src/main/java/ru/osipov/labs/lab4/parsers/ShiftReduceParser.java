package ru.osipov.labs.lab4.parsers;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.LinkedNode;
import ru.osipov.labs.lab3.trees.LinkedTree;
import ru.osipov.labs.lab4.parsers.generators.OPParserGenerator;
import ru.osipov.labs.lab4.parsers.generators.OperatorPresedenceRelations;
import ru.osipov.labs.lab4.parsers.generators.ReduceIndexer;

import java.io.*;
import java.util.Iterator;
import java.util.List;

//Bottom-up parser with shift-reduce.
//Works with Operator Grammars
public class ShiftReduceParser {
    private OperatorPresedenceRelations table;
    private ReduceIndexer rIdx;
    private ILexer lexer;

    public ShiftReduceParser(Grammar G, ILexer lexer){
        OPParserGenerator gen = new OPParserGenerator();
        this.rIdx = gen.getIndicesOfRules(G);
        this.lexer = lexer;
        lexer.setKeywords(G.getKeywords());
        lexer.setOperands(G.getOperands());
        lexer.setAliases(G.getAliases());
        this.table = gen.operatorPresedence(G);
        printTableRelations(G);

    }

    private void printTableRelations(Grammar G){
        System.out.println("Matrix was built.");
        System.out.print("\t\t");
        System.out.print("| ");
        for(String t: G.getTerminals()){
            System.out.print(t+" | ");
        }
        System.out.println("$ |");
        for(String rt: G.getTerminals()){
            System.out.print("| "+rt);
            for(String ct : G.getTerminals()){
                int r = table.getMatrixIndices().get(rt);
                int c = table.getMatrixIndices().get(ct);
                System.out.print(" | "+table.getMatrix()[r][c]);
            }
            System.out.println(" |");
        }
    }

    //Bottom-up parser:: shift-reduce.
    //Operates with operators Grammar.
    public LinkedTree<Token> parse(Grammar G, String fname){
        FileInputStream f;
        boolean isParsed = false;
        try {
            f = new FileInputStream(new File(fname).getAbsolutePath());
            InputStreamReader ch = new InputStreamReader(f);

            LinkedStack<LinkedNode<Token>> S = new LinkedStack<>();
            LinkedNode<Token> EOF = new LinkedNode<>();
            EOF.setValue(new Token("$","$",'t'));
            Iterator<LinkedNode<Token>> S_iter = S.iterator();

            Token tok = lexer.recognize(f);//get token from the input.

            while(true){
                //Get first term on the top of the stack.
                Token X = S.top().getValue();
                while(X.getType() != 't' && S_iter.hasNext()){//If non-terms on the top of the stack => ignore them.
                    X = S_iter.next().getValue();
                }
                S_iter = S.iterator();//restart iterator.
                if(X.getName().equals("$") && tok.getName().equals("$")){
                    isParsed = true;
                    break;
                }
                int idx1 = table.getMatrixIndices().get(X.getName());
                int idx2 = table.getMatrixIndices().get(tok.getName());
                char rel = table.getMatrix()[idx1][idx2];
                if(rel == '<' || rel == '='){//shift
                    LinkedNode<Token> ci = new LinkedNode<>();
                    ci.setValue(tok);
                    S.push(ci);
                    tok = lexer.recognize(f);
                }
                else if(rel == '>'){//reduce
                    LinkedNode<Token> parent = new LinkedNode<>();
                    LinkedNode<Token> child_a = S.top();
                    child_a.setParent(parent);
                    parent.getChildren().add(child_a);
                    while(!S.isEmpty()){
                        S.pop();
                        LinkedNode<Token> child_b = S.top();
                        S.pop();
                        if(child_b.getValue().getType() == 'n'){//add non-terminal too
                            child_b.setParent(parent);
                            parent.getChildren().add(child_b);
                            continue;
                        }
                        else{
                            int r = table.getMatrixIndices().get(child_a.getValue().getName());
                            int c = table.getMatrixIndices().get(child_b.getValue().getName());
                            if(table.getMatrix()[r][c] == '='){//is base row.
                                child_b.setParent(parent);
                                parent.getChildren().add(child_b);
                                child_a = child_b;
                            }
                            else{
                                break;
                            }
                        }
                    }
                    List<LinkedNode<Token>> children = parent.getChildren();
                    StringBuilder b = new StringBuilder();
                    for(int li = children.size() - 1; li >= 0; li--){
                        b.append(children.get(li).getValue().getName());
                    }
                    Integer r_i = rIdx.getStates().get(b.toString());
                    String header = rIdx.getHeaders().get(r_i);
                    parent.setValue(new Token(header,header,'n'));
                }
            }
            lexer.reset();//reset column and line counter.
            if(isParsed)
                return new LinkedTree<Token>(S.top());
            else {
                if(tok.getType() != 'e') {
                    System.out.println(lexer.generateError(S.top().getValue().getName(),tok.getLexem()));
                }
                else
                    System.out.println(tok);
            }
            return null;
        }
        catch (FileNotFoundException e){
            System.out.println("File not found. Specify file to read");
            return null;
        } catch (IOException e) {
            System.out.println("File is not available now.");
            return null;
        }
    }
}