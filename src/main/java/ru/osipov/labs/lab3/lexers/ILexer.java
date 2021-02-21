package ru.osipov.labs.lab3.lexers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ILexer {
    Token recognize(InputStream f) throws IOException;
    Token generateError(String s1, String s2);
    void reset();
    void setKeywords(Set<String> s);
    Set<String> getKeywords();
    void setOperands(Set<String> ops);
    Set<String> getOperands();
    void setAliases(Map<String,String> aliases);
    void setIgnorable(Map<String, List<String>> ignorable);
    Map<String,String> getAliases();
    void setCommentLine(String comment);
    void setMlCommentStart(String mlcS);
    void setMlCommentEnd(String mlcE);
    void setIdName(String id);
}
