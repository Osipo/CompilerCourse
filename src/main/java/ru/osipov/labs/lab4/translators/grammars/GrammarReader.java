package ru.osipov.labs.lab4.translators.grammars;

import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab2.grammars.GrammarString;
import ru.osipov.labs.lab2.grammars.GrammarSymbol;
import ru.osipov.labs.lab2.grammars.json.InvalidJsonGrammarException;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.*;

import java.util.*;

//READ Information about Grammar and GrammarMetaInfo
//Information is represented as Parsing TREE
public class GrammarReader implements Action<Node<Token>> {

    //PTREE OF Grammar (json file)
    private LinkedTree<Token> parsed;

    private Grammar G;

    //Regular definitions (used in regexs of "terms")
    private List<String> regularDefs;

    public GrammarReader(){
        this.parsed = null;
        this.regularDefs = null;
    }

    public void readGrammar(LinkedTree<Token> pTree, Grammar G){
        this.parsed = pTree;
        this.regularDefs = new ArrayList<>();
        this.G = G;
        pTree.visit(VisitorMode.PRE, this);

        //FREE MEMORY.
        this.parsed = null;
        this.regularDefs = null;
        this.G = null;
    }

    @Override
    public void perform(Node<Token> arg) {
        LinkedNode<Token> t = (LinkedNode<Token>) arg;
        if(t.getValue().getName().equals("GTERMS")){
            LinkedNode<Token> st = t.getChildren().get(0);
            parsed.visitFrom(VisitorMode.POST,this::readTerms,st);
        }
        else if(t.getValue().getName().equals("GKEYS")){
            LinkedNode<Token> st = t.getChildren().get(0);
            parsed.visitFrom(VisitorMode.POST,this::readKeywords,st);
        }
        else if(t.getValue().getName().equals("GNONTERMS")){
            LinkedNode<Token> st = t.getChildren().get(0);
            parsed.visitFrom(VisitorMode.POST,this::readNonTerms,st);
        }
        else if(t.getValue().getName().equals("GPRODS")){
            LinkedNode<Token> st = t.getChildren().get(0);
            parsed.visitFrom(VisitorMode.POST,this::readProductions,st);
        }
        else if(t.getValue().getName().equals("GSTART")){
            String s = t.getChildren().get(0).getValue().getLexem();
            G.setStart(removeQuotes(s));
        }
    }

    public void readTerms(Node<Token> st){
        LinkedNode<Token> c = (LinkedNode<Token>) st;
        //READ MEMBER OF OBJECT "terms"
        if(c.getValue().getName().equals("MEMBER")){
            String tName = removeQuotes(c.getChildren().get(1).getValue().getLexem());
            String tValue = removeQuotes(c.getChildren().get(0).getValue().getLexem());
            List<String> l = new LinkedList<>();
            G.getTerminals().add(tName);
            if(tValue.equals("null")){
                if(G.getEmpty() != null)
                    throw new InvalidJsonGrammarException("Only one empty-terminal is allowed. Found two or more",null);
                G.setEmpty(tName);
            }
            else if(tValue.equals("")){
                if(G.getEmpty() != null)
                    throw new InvalidJsonGrammarException("Only one empty-terminal is allowed. Found two or more",null);
                G.setEmpty(tName);
            }
            else{
                l.add(tValue);
                G.getLexicalRules().put(tName,l);
            }
        }
    }

    public void readNonTerms(Node<Token> st){
        LinkedNode<Token> c = (LinkedNode<Token>) st;
        if(!c.getValue().getName().equals("ELEMS")){
            G.getNonTerminals().add(removeQuotes(c.getValue().getLexem()));
        }
    }

    public void readKeywords(Node<Token> st){
        LinkedNode<Token> c = (LinkedNode<Token>) st;
        if(!c.getValue().getName().equals("ELEMS")){
            String k = removeQuotes(c.getValue().getLexem());
            G.getTerminals().add(k);
            G.getKeywords().add(k);
            G.getMeta().getKeywords().add(k);
        }
    }

    public void readProductions(Node<Token> st){
        LinkedNode<Token> c = (LinkedNode<Token>) st;

        //IF RIGHT_PART was written AND its an ARRAY
        if(c.getValue().getName().equals("ELEMS")){
            String rName = removeQuotes(c.getParent().getChildren().get(1).getValue().getLexem());
            GrammarString alpha = new GrammarString();

            //BUILD GrammarString
            int l = c.getChildren().size();
            for(int i = l - 1; i >= 0; i--){
                LinkedNode<Token> cs = c.getChildren().get(i);
                String sym = removeQuotes(cs.getValue().getLexem());
                if(G.getNonTerminals().contains(sym)){
                    alpha.addSymbol(new GrammarSymbol('n',sym));
                }
                else if(G.getTerminals().contains(sym)){
                    alpha.addSymbol(new GrammarSymbol('t',sym));
                }
                else
                    throw new InvalidJsonGrammarException("Illegal grammar symbol! " +sym+
                            "\nExpected non-terminal or termnial or keyword of Grammar.",null);
            }
            //GET Rule or add new.
            Set<GrammarString> product_rules = G.getProductions().get(rName);
            if(product_rules == null)
                product_rules = new HashSet<>();
            product_rules.add(alpha);
            G.getProductions().put(rName,product_rules);
        }
        //ELSE IF RIGHT_PART IS STRING.
        else if(c.getParent().getValue().getName().equals("PROD") && c.getParent().getChildren().indexOf(c) == 0){
            String rName = removeQuotes(c.getParent().getChildren().get(1).getValue().getLexem());
            GrammarString alpha = new GrammarString();
            String sym = removeQuotes(c.getParent().getChildren().get(0).getValue().getLexem());

            if(G.getTerminals().contains(sym)){
                alpha.addSymbol(new GrammarSymbol('t',sym));
            }
            else if(G.getNonTerminals().contains(sym)){
                alpha.addSymbol(new GrammarSymbol('n',sym));
            }
            else if(sym.equals("null")){
                alpha.addSymbol(new GrammarSymbol('t',G.getEmpty()));
            }
            else
                throw new InvalidJsonGrammarException("Illegal grammar symbol! " +sym+
                        "\nExpected non-terminal or termnial or keyword of Grammar.",null);

            Set<GrammarString> product_rules = G.getProductions().get(rName);
            if(product_rules == null)
                product_rules = new HashSet<>();
            product_rules.add(alpha);
            G.getProductions().put(rName,product_rules);
        }
    }

    private static String removeQuotes(String s){
        return s.substring(1,s.length() - 1);
    }
}
