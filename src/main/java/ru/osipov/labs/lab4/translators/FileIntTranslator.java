package ru.osipov.labs.lab4.translators;

import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab3.trees.PositionalTree;
import ru.osipov.labs.lab4.semantics.Env;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class FileIntTranslator<T extends PositionalTree<R>,R extends Token> implements IntTranslator<T,R> {

    protected LinkedStack<Env> env;
    protected String idName;
    protected String eName;
    protected Charset codePage;
    protected Map<String,Integer> t_widths;

    public FileIntTranslator(Grammar G){
        this.idName = G.getIdName();
        this.eName = G.getEmpty();
        this.env = new LinkedStack<>();
        this.env.push(new Env(null));
        this.codePage = StandardCharsets.UTF_16;
        this.t_widths = new HashMap<>();
        t_widths.put("int",4);
        t_widths.put("long",8);
        t_widths.put("float",4);
        t_widths.put("double",8);
        t_widths.put("bool",1);
        t_widths.put("char",2);
        t_widths.put("void",1);
    }

    public void addTypeWidth(String t, Integer n){
        t_widths.put(t,n);
    }

    @Override
    public void translate(T tree, String fname){
        try(Writer wr = new OutputStreamWriter(new FileOutputStream(fname),codePage)){
            wr.write(tree.root().getValue().getContent().toString());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
