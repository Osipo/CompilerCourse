package ru.osipov.labs.lab2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab2.jsonParser.SimpleJsonParser;
import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;

import java.util.*;

@SpringBootConfiguration
public class Main implements CommandLineRunner {
    public static void main(String[] args){
        SpringApplication.run(Main.class,args);
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
            Grammar G = new Grammar(ob);
            //System.out.println("G: 2_4_11");
            System.out.println(G);
//            Grammar DG = G.deleteUselessSymbols();
//            System.out.println("Grammar G without useless symbols");
//            System.out.println(DG);
////            //Delete long rules from grammar.
////            Grammar NG = G.getShortenedGrammar();
////            System.out.println("Shortened G");//8 rules.
////            System.out.println(NG);
//////
////              //Delete empty rules from grammar G.
//              Grammar EG1 = G.getNonEmptyWordsGrammar();//13 rules.
//////            //Delete empty rules in shortened G.
////              Grammar EG2 = NG.getNonEmptyWordsGrammar();//11 rules.
//////
//            System.out.println("Get grammar without eps rules from unshortened G.");//13 rules.
//            System.out.println(EG1);
////            System.out.println("SG: same grammar but first it was shortened and then parsed.");
////            System.out.println(EG2);//11 rules.
//////
////            System.out.println("Get shortened grammar from non-shortened parsed G");
////            Grammar EG3 = EG1.getShortenedGrammar();//18 rules.
////            System.out.println(EG3);
//
//            //Enumerate non-terminals (with productions)
//            System.out.println("Indexed Grammar without eps rules");
//            Grammar G_i = EG1.getIndexedGrammar();
//            System.out.println(G_i);
//            System.out.println("---Delete left recursion---");
//            Grammar G_r = Grammar.deleteLeftRecursion(G);
//            System.out.println(G_r);
//            System.out.println("For all non-terms in G compute A =>* B");
//            for(String N : G.getNonTerminals()){
//                System.out.println(N+" : "+G.compute_allGeneratedN(N));
//            }
//            System.out.println("Grammar without rules A -> B (A,B are non-terminals)");
//            System.out.println(G.getNonCycledGrammar());
            Grammar G_r = Grammar.deleteLeftRecursion(G);
            System.out.println(G_r);
            System.out.println("Remove preffixes");
            Grammar G_rd = G_r.deleteLeftFactor();
            System.out.println(G_rd);
            System.out.println("end");

            System.out.println("Delete loops A -> B");
            System.out.println(G_rd.getNonCycledGrammar().getGrammarWithoutEqualRules());

//
//            Grammar G_rde = G_rd.getNonCycledGrammar().getGrammarWithoutEqualRules();
        }
    }
}
