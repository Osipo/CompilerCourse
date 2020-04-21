package ru.osipov.labs.lab3.lexers;

import java.io.IOException;
import java.io.InputStream;

public interface LexerIO {
    int ungetch(char c);
    int getch(InputStream r) throws IOException;
    int getFilech(InputStream r) throws IOException;
    void clear();
    int getLine();
    int getCol();
    void setLine(int l);
    void setCol(int c);
    String getFromBuffer();
}
