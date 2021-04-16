package ru.osipov.labs.lab2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.osipov.labs.TestDFA;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;

import java.util.regex.Pattern;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestNonEmpty.class)
public class TestNonEmpty {

    @Test
    public void test1(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_2_23.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        System.out.println("Shortened Grammar");
        System.out.println(G.getShortenedGrammar());
        int N = G.getNonTerminals().size();
        G = G.getNonEmptyWordsGrammar();
        System.out.println("Target");
        System.out.println(G);
        assert G != null && N == G.getNonTerminals().size() - 1;
        assert G.getProductions().get("S").size() == 8;
        assert G.getProductions().containsKey("S\'");
    }

    //G_2_23.json :
    //Source: 3 rules (including alternatives) 3 terms, 1 non-term.
    //Source -> after deleting eps -> 10 rules (2 non-terms) -> after shorting 18 rules (10 non-terms)
    //Source -> after shorting 7 rules (5 non-terms) -> after deleting eps 12 rules (6 non-terms)
    //N_e : [S] after deleting eps -> [S']
    @Test
    public void test2(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_2_23.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N = G.getNonTerminals().size();
        G = G.getShortenedGrammar().getNonEmptyWordsGrammar();
        System.out.println("Target");
        System.out.println(G);
        assert G != null && N == G.getNonTerminals().size() - 5;
        assert G.getProductions().get("S").size() == 2;
        assert G.getProductions().containsKey("S\'");
    }

    @Test
    public void test3(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_W_E1.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N = G.getNonTerminals().size();
        G = G.getNonEmptyWordsGrammar();
        System.out.println("Target");
        System.out.println(G);
        assert G != null && N == G.getNonTerminals().size();
        assert G != null && G.getProductions().get("S").size() == 8;
    }

    //G_W_E1.json :
    //Source: 6 rules. (including alternatives) 4 terms, 4 non-terms.
    //rules 6, after shortening -> 8 (6 non-terms), after deleting eps -> 11 rules. (6 non-terms)
    //rules 6, after deleting eps -> 13 (4 non-terms), after shortening -> 18 rules. (9 non-terms)
    //N_e: [A,B,C] after deleting eps -> [].
    @Test
    public void test4(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_W_E1.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N = G.getNonTerminals().size();
        G = G.getShortenedGrammar();
        System.out.println(G);
        G = G.getNonEmptyWordsGrammar();
        System.out.println("Target");
        System.out.println(G);
    }

    @Test
    public void test5(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_2_4_11.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N = G.getNonTerminals().size();
        G = G.getNonEmptyWordsGrammar();
        System.out.println("Target");
        System.out.println(G);
        assert G != null && G.getNonTerminals().size() - 1 == N;
        assert G.getProductions().get("S").size() == 7;
        assert G.getProductions().containsKey("S\'");
    }

    //G_2_4_11.json :
    //Source : 7 rules (including alternatives) 3 terms, 4 non-terms.
    //Source -> after deleting eps -> 17 rules (5 non-terms), -> after shorting 18 rules (6 non-terms)
    //Source -> after shorting 8 rules (5 non-terms) -> after deleting eps 16 rules (6 non-terms).
    //N_e: [A,B,S,C] after deleting eps -> [S']
    @Test
    public void test6(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_2_4_11.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N = G.getNonTerminals().size();
        G = G.getShortenedGrammar();
        System.out.println(G);
        G = G.getNonEmptyWordsGrammar();
        System.out.println("Target");
        System.out.println(G);
        assert G != null && N == G.getNonTerminals().size() - 2;
        assert G.getProductions().get("S").size() == 3;
        assert G.getProductions().containsKey("S\'");
    }
}
