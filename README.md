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
          "id" : "[A-Z][A-Za-z0-9_]+",  
          "num" : "[0-9]+",  
          "empty" : null,  
          "realNum" : "[0-9]+(.[0-9]+((E|e)(@+|-)[0-9]+|empty)|empty)",  
          "character" :  "\"_\"",  
          "+" : "+",    
          "-" : "-",  
          "*" : "*",  
          "/" : "/"  
    },   
    "nonTerms" : [  
        "E", "T", "S", "F"  
    ],   
    "productions" : [  
        {"S" : "E"},  
        {"E" : ["E", "+", "T"]}, {"E": "T"},  
        {"T" : ["T", "*", "F"]}  
    ],  
    "start" : "S"  
}
```
 
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
 Example: **regular expressions : `@*`, `@+`** are treated as `*` and `+`. But not **subExpressions**.
 They both match strings : `"+", "*"`.  
 So to use `@` as operand you must type `@@`.
 It is unnecessary to use `@` in regexs with single character. (Expressions like `"a", "+", "*"` with length = 1)  
 Any single character expression is treated as operand.   
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


## Technical requirements.
Compiled on **JDK 1.8.0_161**.
