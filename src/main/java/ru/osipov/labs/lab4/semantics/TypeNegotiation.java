package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.lexers.TokenAttrs;
import ru.osipov.labs.lab3.trees.Node;
import ru.osipov.labs.lab4.translators.SemanticAnalyzer;

public class TypeNegotiation {
    private static SemanticAnalyzer gen;
    public static void setGen(SemanticAnalyzer g){
        gen = g;
    }
    public static boolean maxType(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord() == null || t2.getRecord() == null)
            return false;
        if(!isNumeric(t1.getRecord()) || !isNumeric(t2.getRecord()))
            return false;

        Node<Token> ch;
        //any numeric type can be extended to double.
        if((ch = hasDouble(t1,t2)) != null) {//any, double => double.
            initAttr(ch,t1,t2);
            return true;
        }

        //any numeric type except double can be extended to float.
        if((ch = hasFloat(t1,t2)) != null) {
            initAttr(ch,t1,t2);
            return true;//(any \ double), double => double.
        }

        //any numeric type except double and float can be extended to long.
        if((ch = hasLong(t1,t2)) != null) {//(any \ double), long => long.
            initAttr(ch,t1,t2);
            return true;
        }

        //any numeric type except double, float and long can be extended to int.
        if((ch = hasInt(t1,t2)) != null) {//(any \ double \ long), int => int.
            initAttr(ch,t1,t2);
            return true;
        }
        //any numeric type except double, float and long and int can be extended to char.
        if((ch = hasChar(t1,t2)) != null){//(any \ double \ long), int => int.
            initAttr(ch,t1,t2);
            return true;
        }
        return false;
    }

    //t1 is greater than t2
    public static boolean greaterThan(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord() == null || t2.getRecord() == null)
            return false;
        if(!isNumeric(t1.getRecord()) || !isNumeric(t2.getRecord()))
            return false;

        Node<Token> ch;
        //any numeric type can be extended to double.
        if((ch = hasDouble(t1,t2)) != null) {//any, double => double.
            if(ch.equals(t1)){//t1 > t2
                initAttr(ch,t1,t2);
                return true;
            }
            return false;
        }

        //any numeric type except double can be extended to float.
        if((ch = hasFloat(t1,t2)) != null) {
            if(ch.equals(t1)){//t1 > t2
                initAttr(ch,t1,t2);
                return true;
            }
            return false;
        }

        //any numeric type except double and float can be extended to long.
        if((ch = hasLong(t1,t2)) != null) {//(any \ double), long => long.
            if(ch.equals(t1)){//t1 > t2
                initAttr(ch,t1,t2);
                return true;
            }
            return false;
        }

        //any numeric type except double, float and long can be extended to int.
        if((ch = hasInt(t1,t2)) != null) {//(any \ double \ long), int => int.
            if(ch.equals(t1)){//t1 > t2
                initAttr(ch,t1,t2);
                return true;
            }
            return false;
        }
        //any numeric type except double, float and long and int can be extended to char.
        if((ch = hasChar(t1,t2)) != null){//(any \ double \ long), int => int.
            if(ch.equals(t1)){//t1 > t2
                initAttr(ch,t1,t2);
                return true;
            }
            return false;
        }
        return false;
    }

    //t => node with wider type then other.
    private static void initAttr(Node<Token> t, Node<Token> t1,Node<Token> t2){
        Node<Token> temp = t;//node with narrower type.
        String etype = t.getRecord().getType();
        if(temp.equals(t1)) {
            temp = t2;
        }
        else {
            temp = t1;
        }
        Token v = temp.getValue();
        TokenAttrs code = new TokenAttrs(v);
        gen.incCounter();
        code.setCode("= "+v.getLexem()+" "+etype+" "+gen.getCounter()+"t");
        code.setLexem(gen.getCounter()+"t");
        temp.setValue(code);

        //set new type to parent node! (CHECK node AL :: parent(t) => AL)
        Node<Token> parent = gen.getAnnotatedParsedTree().parent(t);
        if(parent.getRecord() == null){
            parent.setRecord(new Entry(parent.getValue().getName(),etype,EntryCategory.CONSTANT,0));
        }
        else
            parent.getRecord().setType(etype);
    }


    private static Node<Token> hasLong(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord().getType().equals("long"))
            return t1;
        else if(t2.getRecord().getType().equals("long"))
            return t2;
        return null;
    }

    private static Node<Token>  hasDouble(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord().getType().equals("long"))
            return t1;
        else if(t2.getRecord().getType().equals("long"))
            return t2;
        return null;
    }

    private static Node<Token>  hasFloat(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord().getType().equals("long"))
            return t1;
        else if(t2.getRecord().getType().equals("long"))
            return t2;
        return null;
    }

    private static Node<Token>  hasInt(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord().getType().equals("long"))
            return t1;
        else if(t2.getRecord().getType().equals("long"))
            return t2;
        return null;
    }

    private static Node<Token>  hasChar(Node<Token> t1, Node<Token> t2){
        if(t1.getRecord().getType().equals("char"))
            return t1;
        else if(t2.getRecord().getType().equals("char"))
            return t2;
        return null;
    }

    public static boolean isNumeric(Entry t){
        if(t == null)
            return false;
        String type = t.getType();
        return type.equals("int") || type.equals("long") || type.equals("float") || type.equals("double")
                || type.equals("char") || type.equals("bool");
    }
}
