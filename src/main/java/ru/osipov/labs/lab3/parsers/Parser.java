package ru.osipov.labs.lab3.parsers;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import ru.osipov.labs.lab2.grammars.Grammar;
import ru.osipov.labs.lab3.lexers.ILexer;
import ru.osipov.labs.lab3.lexers.Token;
import ru.osipov.labs.lab4.semantics.Entry;
import ru.osipov.labs.lab4.semantics.EntryCategory;
import ru.osipov.labs.lab4.semantics.Env;
import ru.osipov.labs.lab4.semantics.SInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Parser {
    protected ILexer lexer;
    protected String empty;//empty symbol of grammar.
    protected boolean isParsed;

    protected ParserMode mode;

    public void setParserMode(ParserMode mode){
        this.mode = mode;
    }


    public Parser(Grammar G, ILexer lexer) {
        this.lexer = lexer;
        this.isParsed = true;
        this.empty = G.getEmpty();
        lexer.setKeywords(G.getKeywords());
        lexer.setOperands(G.getOperands());
        lexer.setAliases(G.getAliases());
        lexer.setCommentLine(G.getCommentLine());
        lexer.setMlCommentStart(G.getMlCommentStart());
        lexer.setMlCommentEnd(G.getMlCommentEnd());
        lexer.setIdName(G.getIdName());
        lexer.setIgnorable(G.getIgnorable());
        this.mode = ParserMode.HIDE;
    }
}
