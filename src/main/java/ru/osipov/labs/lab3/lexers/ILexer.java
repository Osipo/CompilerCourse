package ru.osipov.labs.lab3.lexers;

import java.io.IOException;
import java.io.InputStream;

public interface ILexer {
    Token recognize(InputStream f) throws IOException;
}
