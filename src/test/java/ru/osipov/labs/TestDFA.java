package ru.osipov.labs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.osipov.labs.lab1.Main;
import ru.osipov.labs.lab1.structures.automats.DFA;
import ru.osipov.labs.lab1.structures.automats.NFA;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab1.utils.RegexRPNParser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TestDFA.class)
public class TestDFA {

    @Test
    public void test1(){
        RegexRPNParser parser = new RegexRPNParser();
        String expr = "(a|b)*abb";
        expr = Main.addConcat(expr,parser);
        LinkedStack<Character> postfix = parser.GetInput(expr);
        NFA nfa = Main.buildNFA(postfix,parser);
        DFA dfa = new DFA(nfa);
        DFA mindfa = new DFA(dfa);
        assert mindfa.Recognize("abb") && dfa.Recognize("abb") && nfa.Recognize("abb");

    }

    @Test
    public void test2(){
        RegexRPNParser parser = new RegexRPNParser();
        String expr = "(ab|cd)+abb";
        expr = Main.addConcat(expr,parser);
        LinkedStack<Character> postfix = parser.GetInput(expr);
        NFA nfa = Main.buildNFA(postfix,parser);
        DFA dfa = new DFA(nfa);
        DFA mindfa = new DFA(dfa);
        assert !mindfa.Recognize("abb");
        assert mindfa.Recognize("abababb") && mindfa.Recognize("abcdabb");
    }

    @Test
    public void test3(){
        RegexRPNParser parser = new RegexRPNParser();
        String expr = "(a|c|z)*";
        expr = Main.addConcat(expr,parser);
        LinkedStack<Character> postfix = parser.GetInput(expr);
        System.out.println(postfix);
        NFA nfa = Main.buildNFA(postfix,parser);
        DFA dfa = new DFA(nfa);
        DFA mindfa = new DFA(dfa);
        assert mindfa.Recognize("zzazzcz");
        assert mindfa.Recognize("");
    }
}