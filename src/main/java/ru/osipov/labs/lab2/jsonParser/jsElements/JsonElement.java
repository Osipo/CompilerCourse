package ru.osipov.labs.lab2.jsonParser.jsElements;

import java.util.ArrayList;

public abstract class JsonElement<T> {
    public abstract T getValue();

    @Override
    public String toString(){
        return getValue().toString();
    }

}
