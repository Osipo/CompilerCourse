package ru.osipov.labs.lab2.jsonParser;

public class JsonParseException extends RuntimeException {
    public JsonParseException(String message, Throwable throwable){
        super(message,throwable);
    }
}
