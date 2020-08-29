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

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= TestLeftRecursion.class)
public class TestLeftRecursion {
    @Test
    public void testRec1() {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_2_27.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N1 = G.getNonTerminals().size();
        G = Grammar.deleteLeftRecursion(G);
        System.out.println("Target");//Step 1: A -> aAA'1, | aBc, A'1 -> B | c, Step 2: A -> aA'2, A'2 -> AA'1 | Bc.
        System.out.println(G);
        assert G.getN_e().size() == 0 && G.getNonTerminals().size() == N1 + 2;
    }

    @Test
    public void testRec2(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\G_2_27.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N1 = G.getNonTerminals().size();
        G = G.getNonCycledGrammar();
        int N2 = G.getNonTerminals().size();
        System.out.println("_______ without chained rules");
        System.out.println(G);
        System.out.println("Target");
        G = Grammar.deleteLeftRecursion(G);
        System.out.println(G);
        int N3 = G.getNonTerminals().size();
        G = G.deleteLeftFactor();
        System.out.println("Delete preffix");
        System.out.println(G);
        int N4 = G.getNonTerminals().size();
        System.out.println(N1+" : "+N2+" : "+N3+" : "+N4);
    }
}
