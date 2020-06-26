package ru.osipov.labs.lab4.translators;

import ru.osipov.labs.lab3.trees.PositionalTree;

import java.io.FileWriter;

public interface IntTranslator<T extends PositionalTree<R>,R> {
    void translate(T tree, String fname);
}
