package ru.osipov.labs.lab2.jsonParser.jsElements;

public class JsonNull extends JsonElement<String> {
    @Override
    public String getValue() {
        return "null";
    }
}
