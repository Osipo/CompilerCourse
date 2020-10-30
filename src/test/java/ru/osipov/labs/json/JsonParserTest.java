package ru.osipov.labs.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;
import ru.osipov.labs.lab3.TestYmlG;

@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= JsonParserTest.class)
public class JsonParserTest {
    
    @Test
    public void testSomeData(){
        System.out.println("Current working dir: "+System.getProperty("user.dir"));
        String s = System.getProperty("user.dir")+"\\src\\test\\java\\ru\\osipov\\labs\\json\\";
        s = s + "input\\SomeData.json";
        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(s);
        assert ob != null;
        System.out.println(ob);
    }
}
