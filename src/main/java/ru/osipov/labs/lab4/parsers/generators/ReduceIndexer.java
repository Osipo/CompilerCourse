package ru.osipov.labs.lab4.parsers.generators;

import java.util.Map;

public class ReduceIndexer {
    private Map<String,Integer> states;
    private Map<Integer,String> headers;

    public ReduceIndexer(Map<String,Integer> states, Map<Integer,String> headers){
        this.states = states;
        this.headers = headers;
    }

    public Map<String, Integer> getStates() {
        return states;
    }

    public Map<Integer, String> getHeaders() {
        return headers;
    }
}
