package ru.osipov.labs.lab2.grammars.json;

public class InvalidJsonGrammarException extends RuntimeException {
    public InvalidJsonGrammarException(String message, Throwable throwable){
        super(message,throwable);
    }
}
