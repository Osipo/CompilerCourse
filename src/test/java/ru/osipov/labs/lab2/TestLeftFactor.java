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
@ContextConfiguration(classes= TestLeftFactor.class)
public class TestLeftFactor {
    @Test
    public void testFactor1() {
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\testFactor_1.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        int N1 = G.getNonTerminals().size();
        G = G.deleteLeftFactor();
        assert G != null;
        System.out.println(G);
        assert G.getN_e().size() == 0 && G.getNonTerminals().size() == N1 + 2;
    }

    @Test
    public void testFactor_2(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\testFactor_2.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        int N1 = G.getNonTerminals().size();
        G = G.deleteLeftFactor();
        assert G != null;
        System.out.println(G);
        assert G.getN_e().size() != 0 && G.getNonTerminals().size() == N1 + 1;
    }

    @Test
    public void testFactor_3(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\testFactor_3.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        int N1 = G.getNonTerminals().size();
        G = G.deleteLeftFactor();
        assert G != null;
        System.out.println(G);
        assert G.getN_e().size() != 0 && G.getNonTerminals().size() == N1 + 3;
    }

    @Test
    public void testFactor_4(){
        String p = System.getProperty("user.dir");
        System.out.println(p);
        p = p+"\\src\\test\\java\\ru\\osipov\\labs\\lab2\\";
        p = p+"grammarJson\\testFactor_4.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        assert ob != null;
        Grammar G = new Grammar(ob);
        System.out.println("Source");
        System.out.println(G);
        int N1 = G.getNonTerminals().size();
        G = G.deleteLeftFactor();
        assert G != null;
        System.out.println("Target");
        System.out.println(G);
        assert G.getN_e().size() == 0 && G.getNonTerminals().size() == N1 + 2;
    }

}
