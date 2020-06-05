package ru.osipov.labs.lab4.parsers.generators;

import java.util.Map;

public class OperatorPresedenceRelations {
    private char[][] matrix;
    private Map<String,Integer> idx;

    public OperatorPresedenceRelations(char[][] matrix,Map<String,Integer> mIdx){
        this.idx = mIdx;
        this.matrix = matrix;

    }

    public char[][] getMatrix() {
        return matrix;
    }

    public Map<String, Integer> getMatrixIndices() {
        return idx;
    }
}
