package ru.osipov.labs.lab1.utils;

import java.util.*;

public class ColUtils {
    public static <T extends Collection<R>,R> R getElemOfSet(T col,int idx){
        int s = 0;
        for (R f : col) {
            if (s == idx)
                return f;
            s++;
        }
        return null;
    }

    public static <T> List<T> fromSet(Set<T> col){
        ArrayList<T> result = new ArrayList<>();
        Iterator<T> itr = col.iterator();
        while(itr.hasNext())
            result.add(itr.next());
        return result;
    }
}
