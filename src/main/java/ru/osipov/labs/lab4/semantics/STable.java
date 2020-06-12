package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab3.trees.BinarySearchTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class STable {
    private ArrayList<BinarySearchTree<SInfo>> table;

    private final int HASH_MIN;
    private final int HASH_MAX;

    public STable(int hmin, int hmax){
        this.HASH_MIN = hmin;
        this.HASH_MAX = hmax;
        int l = hmax - hmin + 1;
        this.table = new ArrayList<>(l);
        for(int i = 0; i < l; i++){
            table.add(null);
        }
        System.out.println("Table was initiated with "+table.size()+" elements.");
    }

    public void clear(){
        int l = HASH_MAX - HASH_MIN + 1;
        for(int i = 0; i < l; i++){
            this.table.set(i,null);
        }
        this.table = null;
        System.out.println("Table was clear.");
    }
    public void init(){
        int l = HASH_MAX - HASH_MIN + 1;
        this.table = new ArrayList<>(l);
        for(int i = 0; i < l; i++){
            table.add(null);
        }
        System.out.println("Table was initiated with "+table.size()+" elements.");
    }

    public void add(String s){
        int h = hash(s);
        BinarySearchTree<SInfo> rec = table.get(h);
        if(rec == null){
            rec = new BinarySearchTree<SInfo>(new StringContainerComparator<SInfo>());
            rec.add(new SInfo(s));
        }
        else{
            rec.add(new SInfo(s));
        }
    }

    private int hash(String s){
        int l = (s.length() + 1) / 2;
        int result = ((int)s.charAt(0) + (int)s.charAt(l) - HASH_MIN) % (HASH_MAX - HASH_MIN + 1) + HASH_MIN;
        if(result < HASH_MIN)
            result = HASH_MIN;
        return result;
    }
}
