# Compiler Generator
## Descirption
Consumes Grammar.json file which contains definition of Context Free Grammar (CFG). The structure of JSON file is given
in Section Grammar Structure.

## Build
Open cmd (or bash) go to the root of the project (directory CompilerCoursePr) and execute:
```
gradlew bootJar
```
The built application will be appeared at **./build/libs**

## Usage
The built app (jar file) is located at directory **./build/libs**  
First of all you must LOAD the **Grammar**. The **Grammar**
defines a 4 elements of CFG:  **Terminals**, **NonTerminals**,  **Productions**,  **StartSymbol**.  
You can load a predefined Grammar files in follow directory  
```
/CompilerCoursePr/src/main/java/ru/osipov/labs/lab2/grammars/json
``` 
After that you must specify txt fileName for analysis and compilation.   
### Grammar Structure
The structure of json document is described as follows
>  
```JSON
{  
    "terms" : {  
          "name" : "[A-Z][A-Za-z0-9_]+",  
          "num" : "[0-9]+",  
          "empty" : null,  
          "realNum" : "[0-9]+(.[0-9]+((E|e)(@+|-)[0-9]+|empty)|empty)",  
          "character" :  "\"_\"",  
          "+" : "+",    
          "-" : "-",  
          "*" : "*",  
          "/" : "/",  
          "(" : "(",  
          ")" : ")",  
          "//" : "//",  
          "/*" : "/@*"  
    },   
    "nonTerms" : [  
        "E", "T", "S", "F"  
    ],   
    "productions" : [  
        {"S" : "E"},  
        {"E" : ["E", "+", "T"]}, {"E" : ["E", "-", "T"]}, {"E": "T"},  
        {"T" : ["T", "*", "F"]}, {"T" : ["T", "/", "F"]}, {"T" : "F"},  
        {"F" : ["(","name",")"]}, {"F" : "E"}  
    ],  
    "start" : "S",  
    "keywords" : ["true", "false", "null"],  
    "meta" : {  
        "id" : "name",  
        "commentLine" : "//",  
        "mlCommentStart" : "/*",  
        "mlCommentEnd" : "*/",  
        "scopeStart": "{",  
        "scopeEnd": "}",  
        "operands": ["name","num","realNum","true","false","character"],  
        "operators": ["+","-","*","/"],  
        "aliases": {  
          "-": "um",  
          "+": "up"  
        },  
        "types": [  
          {"name": "double","size": 8},  
          {"name": "long","size": 8},  
          {"name": "float", "size": 4},  
          {"name": "int","size": 4},  
          {"name": "char","size": 2},  
          {"name": "bool","size": 1},  
          {"name": "void","size": 0}  
        ]  
    }  
}
```

### Keywords and meta data
You can set additional metadata to your Grammar.
 1. **"keywords"** : list of strings which represent keywords of language.  
 Each ***keyword*** is treated as a terminal with same pattern as its name.  
 When you are defining **keywords** you must ensure that overlapped pattern exists.  
 Also, each character which is part of **keyword** MUST BE IN patterns (***regular expressions***)  
 2. **"meta"** : JsonObject  which contains additional data for semantic analysis:  
    2.1 **"id"** : The name of terminal, which represent an id - entry of symbol table.  
    2.2 **"commentLine"** : The name of terminal, which represents the begining  
    of the single line comment. Comments are ignored by parser.  
    2.3 **"mlCommentStart"** : The name of terminal which represents the begining  
    of the multiline comment.  
    2.4 **"mlCommentEnd"** : The string which marks the end of the  
    multiline comment.  
    2.5 **"operands"** : The list of strings. Each element is a terminalName  
    (property name of "terms" object). This property is used for semantic Analysis.  
    2.6 **"operators"** : The list of strings. Each element is a terminalName  
    (property name of "terms" object). This property is used for semantic Analysis.  

### Regex syntax
The ***regular expressions*** syntax is described as follows: 
 - `a` - *regular expression* which represent a single character string ("a").  
 (You may type a multiple character sequence `abb...`. An operator of CONCATENATION `^` will be used implicitly.)  
 (So if you type a `abb` the regex will be `a^b^b`. Of course you can use `^` operator explicitly BUT THIS IS NOT RECOMMENDED!) 
 - `r1|r2` - the UNION of two ***regular expressions (r1 and r2)***.
 - `[A-Z]` - the characters class. It is shortance for UNION operation. (i.e it is equal to expression (A|B|...|Z)    ).
 - `(r)` - the GROUPING of **regex (r1)**.
 - `_` - any single character. (The character code range is [0..65535])
 - `@` - escape symbol. (like a `\` in many programming languages).   
 Use to ignore operators (such as `^`, `|`, `(`, `)`,`*`,`+`,`_`).
 Any single character preceded with `@` is treated as an operand  
 Example: **regular expressions : `@*`, `@+`** are treated as `*` and `+`.  
 They both match strings : `"+", "*"`.  
 So to use `@` as operand you must type `@@`.
 It is unnecessary to use `@` in regexs with single character. (Expressions like `"a", "+", "*"` with length = 1)  
 Any single character expression (i.e. full expression) is treated as operand.  
 WARNING: the length of the expression is the length of the JsonString  
 (** i.e. the length of the string which is a value of property `"pattern".    
 - null - regular expression which matches **empty strings**.  

#### Regex quantifiers
There are only two quantifiers are used `*` and `+` which means:
 - `r1+` : one or more times exactly r1.
 - `r1*` : zero or more times exactly r1.

#### Regex details
After all regexs will be processed the minimal DFA will be built.  
Technically this is not formally the DFA. It is based on model  
used in **Lex, JFLex and YACC** programs.  
WARNING: All **regular expressions** are **CASE SENSITIVE**.  

There are reserved two character codes:
 - 0 - character code, which means **'_'** ,  
 regular expression which matches any single character strings.  
 - 1 - character code, which means **null**.  

You can use _terminalName_ which has **null expression**  
in other regular expressions. 


## Technical requirements.
Compiled on **JDK 1.8.0_161**.
