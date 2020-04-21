package ru.osipov.labs.lab1.utils;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

public class RegexRPNParser {
    public RegexRPNParser(){ }
    private char[] terminals = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public boolean isTerminal(char c){
        for(int i = 0; i < terminals.length;i++){
            if(terminals[i] == c)
                return true;
        }
        return false;
    }

    //Set array of t
    public void setTerminals(char[] t){
        this.terminals = t;
    }

    //Set terms of regex.
    public void setTerminals(Set<Character> symbols){
        terminals = new char[symbols.size()];
        int i = 0;
        for(Character c : symbols){
            terminals[i++] = c;
        }
    }


    public boolean isOperator(Character c) {
        switch (c) {
            case '*': case '+':return true;
            case '|': return true;
            case '(': return true;
            case ')': return true;
            case '^':return true;
            default:return false;
        }
    }

    public boolean isUnaryOp(Character c){
        return c == '*' || c == '+';
    }

    /*Priority of operator.*/
    private int getPriority(Character c) {
        switch (c) {
            case '+': case '*':return 3;
            case '^': return 2;
            case '|': return 1;
            case '(': case ')': return 0;
            default: return -1;
        }
    }

    public LinkedStack<Character> GetInput(String s){
        LinkedStack<Character> ops = new LinkedStack<Character>();
        LinkedStack<Character> rpn = new LinkedStack<Character>();
        for(int i = 0; i < s.length(); i++){
            char tok = s.charAt(i);
            if(tok == '('){
                ops.push(tok);
            }
            else if(tok == ')'){
                while(!ops.isEmpty() && ops.top() != '('){
                    rpn.push(ops.top());
                    ops.pop();
                }
                ops.pop();
            }
            else if(!isOperator(tok) && isTerminal(tok)){
                rpn.push(tok);
            }
            else if(isOperator(tok)) {
                while (!ops.isEmpty() && isOperator(ops.top()) && getPriority(tok) <= getPriority(ops.top())) {
                    rpn.push(ops.top());
                    ops.pop();
                }
                ops.push(tok);
            }
            else if(!isOperator(tok) && !isTerminal(tok)) {
                return null;
            }
        }
        while(!ops.isEmpty()){
            rpn.push(ops.top());
            ops.pop();
        }
        LinkedStack<Character> result = new LinkedStack<Character>();
        while(!rpn.isEmpty()){
            result.push(rpn.top());
            rpn.pop();
        }
        return result;
    }
}
