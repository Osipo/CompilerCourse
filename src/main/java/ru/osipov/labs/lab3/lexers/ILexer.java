package ru.osipov.labs.lab3.lexers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface ILexer {
    Token recognize(InputStream f) throws IOException;
    Token generateError(String s1, String s2);
    void reset();
    void setKeywords(Set<String> s);
    Set<String> getKeywords();
}
