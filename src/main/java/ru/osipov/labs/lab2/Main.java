package ru.osipov.labs.lab2;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;

import java.util.*;

@SpringBootConfiguration
public class Main implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication app = new SpringApplication(Main.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        String p = System.getProperty("user.dir");
        System.out.println("Current working dir: "+p);
        p = p + "\\src\\main\\java\\ru\\osipov\\labs\\lab2\\";
        p = p + "grammars\\json\\G_2_27.json";

        SimpleJsonParser parser = new SimpleJsonParser();
        JsonObject ob = parser.parse(p);
        //G_W_E1.json :
        //Source: 6 rules. (including alternatives) 4 terms, 4 non-terms.
        //rules 6, after shortening -> 8 (6 non-terms), after deleting eps -> 11 rules. (6 non-terms)
        //rules 6, after deleting eps -> 13 (4 non-terms), after shortening -> 18 rules. (9 non-terms)
        //N_e: [A,B,C] after deleting eps -> [].

        //G_2_4_11.json :
        //Source : 7 rules (including alternatives) 3 terms, 4 non-terms.
        //Source -> after deleting eps -> 17 rules (5 non-terms), -> after shorting 18 rules (6 non-terms)
        //Source -> after shorting 8 rules (5 non-terms) -> after deleting eps 16 rules (6 non-terms).
        //N_e: [A,B,S,C] after deleting eps -> [S']

        //G_2_23.json :
        //Source: 3 rules (including alternatives) 3 terms, 1 non-term.
        //Source -> after deleting eps -> 10 rules (2 non-terms) -> after shorting 18 rules (10 non-terms)
        //Source -> after shorting 7 rules (5 non-terms) -> after deleting eps 12 rules (6 non-terms)
        //N_e : [S] after deleting eps -> [S']

        //G_2_9.json :
        //Source 7 rules 3 terms  4 non-terms.
        //Source -> after deleting eps -> 8 rules (5 non-terms),
        //Source -> after shorting nothing was changed, after deleting eps -> 8 rules, 5 non-terms.
        //N_e: [S] after deleting eps -> [S']

        if (ob != null) {
            System.out.println("Json was read successfull.");
            Grammar G = null;
            try {
                 G = new Grammar(ob);
            }
            catch (InvalidJsonGrammarException e){
                System.out.println(e.getMessage());
                System.exit(-2);
            }
            //System.out.println("_______: 2_4_11");
            System.out.println(G);
            System.out.println("Delete left recursion");
            G = Grammar.deleteLeftRecursion(G);
            System.out.println(G);
            System.out.println("Delete preffixes");
            System.out.println(G.deleteLeftFactor());
//            Grammar DG = _______.deleteUselessSymbols();
//            System.out.println("Grammar _______ without useless symbols");
//            System.out.println(DG);
////            //Delete long rules from grammar.
////            Grammar NG = _______.getShortenedGrammar();
////            System.out.println("Shortened _______");//8 rules.
////            System.out.println(NG);
//////
////              //Delete empty rules from grammar _______.
//              Grammar EG1 = _______.getNonEmptyWordsGrammar();//13 rules.
//////            //Delete empty rules in shortened _______.
////              Grammar EG2 = NG.getNonEmptyWordsGrammar();//11 rules.
//////
//            System.out.println("Get grammar without eps rules from unshortened _______.");//13 rules.
//            System.out.println(EG1);
////            System.out.println("SG: same grammar but first it was shortened and then parsed.");
////            System.out.println(EG2);//11 rules.
//////
////            System.out.println("Get shortened grammar from non-shortened parsed _______");
////            Grammar EG3 = EG1.getShortenedGrammar();//18 rules.
////            System.out.println(EG3);
//
//            //Enumerate non-terminals (with productions)
//            System.out.println("Indexed Grammar without eps rules");
//            Grammar G_i = EG1.getIndexedGrammar();
//            System.out.println(G_i);
//            System.out.println("---Delete left recursion---");
//            Grammar G_r = Grammar.deleteLeftRecursion(_______);
//            System.out.println(G_r);
//            System.out.println("For all non-terms in _______ compute A =>* B");
//            for(String N : _______.getNonTerminals()){
//                System.out.println(N+" : "+_______.compute_allGeneratedN(N));
//            }
//            System.out.println("Grammar without rules A -> B (A,B are non-terminals)");
//            System.out.println(_______.getNonCycledGrammar());
            //_______ = _______.getNonCycledGrammar();
           // System.out.println("delete cycles");
           // System.out.println(_______);
        }
        else{
            System.out.println("Invalid json. Json document is required!");
            System.exit(-3);
        }
    }
}
