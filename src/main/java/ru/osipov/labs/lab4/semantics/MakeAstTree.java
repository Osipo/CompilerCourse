package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.*;

import java.util.Set;

//From LL-tree.
public class MakeAstTree implements Action<Node<Token>> {

    private Set<String> operands;

    private Set<String> operators;

    private Set<String> kws;

    private String scopeS;

    private String scopeE;

    public MakeAstTree(Grammar G){
        this.operands = G.getOperands();
        this.operators = G.getOperators();
        this.kws = G.getKeywords();
        this.scopeS = G.getScopeBegin();
        this.scopeE = G.getScopeEnd();
    }

    @Override
    public void perform(Node<Token> arg) {
       LinkedNode<Token> current = (LinkedNode<Token>) arg;
        System.out.println(current.getValue().getName());
       for (int i = 0; i < current.getChildren().size(); i++) {
           LinkedNode<Token> c = current.getChildren().get(i);
           String name = c.getValue().getName();
           if (c.getValue().getType() == 't' && !(operands.contains(name) || operators.contains(name) || kws.contains(name) || scopeS.equals(name) || scopeE.equals(name))) {
               current.getChildren().remove(c);
               c.setParent(null);
               i = -1;
           }
           //remove nonTerm if it is useless. And up to 1 level its children.
           else if(c.getValue().getType() != 't'  && operands.contains(c.getValue().getName())){
               current.getChildren().remove(c);
               c.setParent(null);
               i = -1;
               for(LinkedNode<Token> c2 : c.getChildren()){
                   c2.setParent(current);
                   current.getChildren().add(c2);
               }
               //c.setChildren(null);
           }
           else if(operators.contains(c.getValue().getName())){
               c.setParent(null);
               current.getChildren().remove(c);
               current.setValue(c.getValue());
           }
       }
    }
}