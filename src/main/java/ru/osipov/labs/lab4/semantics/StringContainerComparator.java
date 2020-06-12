package ru.osipov.labs.lab4.semantics;

import java.util.Comparator;

public class StringContainerComparator<C extends Value<String>> implements Comparator<C> {
    @Override
    public int compare(C obj1, C obj2) {
        if(obj1 == null && obj2 == null)
            return 0;
        if (obj1 == null) {
            return -1;
        }
        if (obj2 == null) {
            return 1;
        }
        String arg1 = obj1.getValue();
        String arg2 = obj2.getValue();
        if(arg1 == null && arg2 == null)
            return 0;
        if (arg1 == null) {
            return -1;
        }
        if (arg2 == null) {
            return 1;
        }

        if (arg1.equals(arg2)) {
            return 0;
        }
        return arg1.compareTo(arg2);
    }
}
