package ru.osipov.labs.lab2.jsonParser;

public enum JsParserState {
    START,OPENROOT,CLOSEROOT,OPENBRACE,CLOSEBRACE,OPENARR,ARRELEM,CLOSEARR,OPENQ,CLOSEQ,OPENQP,CLOSEQP,COLON,ERR,ID_READ;
}
