package ru.osipov.labs.lab4.semantics;

import ru.osipov.labs.lab3.trees.BinarySearchTree;
import ru.osipov.labs.lab3.trees.PrintlnAction;
import ru.osipov.labs.lab3.trees.VisitorMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class STable {
    private ArrayList<BinarySearchTree<SInfo>> table;

    private final int HASH_MIN;
    private final int HASH_MAX;

    public STable(){
        this(0,65535 * 2);
    }

    public STable(int hmin, int hmax){
        this.HASH_MIN = hmin;
        this.HASH_MAX = hmax;
        init();
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
            this.table.add(null);
        }
        System.out.println("Table was initiated with "+table.size()+" null elements.");
    }

    public void add(SInfo entry){
        int h = hash(entry.getValue());
        BinarySearchTree<SInfo> rec = table.get(h);
        if(rec == null){
            rec = new BinarySearchTree<SInfo>(new StringContainerComparator<SInfo>());
            rec.add(entry);
            this.table.set(h,rec);
        }
        else{
            rec.add(entry);
        }
    }

    public void add(String s){
        int h = hash(s);
        BinarySearchTree<SInfo> rec = table.get(h);
        if(rec == null){
            rec = new BinarySearchTree<SInfo>(new StringContainerComparator<SInfo>());
            rec.add(new SInfo(s));
            this.table.set(h,rec);
        }
        else{
            rec.add(new SInfo(s));
        }
    }

    public void printTable(){
        for(BinarySearchTree<SInfo> t : table){
            if(t != null)
                t.visit(VisitorMode.IN,new PrintlnAction<SInfo>());
        }
    }

    public SInfo get(String s){
        SInfo it = new SInfo(s);//for type negotiation. (Only str in SInfo are compared)
        int h = hash(s);
//        System.out.println("Hash: "+h);
//        System.out.println("Str: "+s);
        BinarySearchTree<SInfo> rec = table.get(h);
        if(rec == null)
            return null;//no record in hashTable.
        return rec.get(it);//returns SInfo from tree. (not from variable it!).
    }

    private int hash(String s){

        int l = (s.length() ) / 2;
        int result = ((int)s.charAt(0) + (int)s.charAt(l) - HASH_MIN) % (HASH_MAX - HASH_MIN + 1) + HASH_MIN;
        if(result < HASH_MIN)
            result = HASH_MIN;
        return result;
    }
}
