package ru.osipov.labs.lab2.jsonParser;

import ru.osipov.labs.lab1.structures.graphs.Pair;
import ru.osipov.labs.lab2.jsonParser.jsElements.*;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

//PARSE JSON Documents.
//NOTE: Only rooted Json Document (i.e. JsonText in JsonDocument bounded with object)
// (File with single string: "something" or with single literal (true, 12.5) IS NOT VALID)
// (VALID File is JSON Object with its properties)
public class SimpleJsonParser {
    private JsParserState state;
    private char[] buf;
    private int bsize;
    private int bufp;
    private int EOL;

    private int line;
    private int col;
    private boolean escaped;

    private LinkedStack<JsParserState> S1;

    public SimpleJsonParser(int bsize){
        this.state = JsParserState.START;
        this.buf = new char[bsize];
        this.bsize = bsize;
        this.bufp = 0;

        this.EOL = 0;
        this.line = 1;
        this.col = 0;
        this.S1 = new LinkedStack<>();
        this.escaped = false;
    }

   public SimpleJsonParser(){
        this(255);
   }


   public JsonObject parseStream(InputStream in){
        LinkedStack<JsonObject> obs = new LinkedStack<>();
        obs.push(new JsonObject());
        LinkedStack<JsonArray> arrays = new LinkedStack<>();
        try {
            int c = in.read();
            if((char)c == '{') {
                state = JsParserState.OPENROOT;
                this.S1.push(state);
                col++;
            }
            else
                err((char)c,"{");//root not found!
            JsonString pname = new JsonString("");
            JsonString pflag = new JsonString("");
            while(state != JsParserState.CLOSEROOT && state != JsParserState.ERR && !S1.isEmpty()){//read characters until last '}' or error is found.
                char cur;
                while((cur = (char)getch(in)) == ' ' || cur == '\t' || cur == '\n' || cur == '\r');
                changeState(state,cur,in,obs,arrays,pname,pflag);
            }
            in.close();
            if(state == JsParserState.ERR)
                return null;
        } catch (IOException e) {
            System.out.println("Stream is not available now!");
            return null;
        } catch (JsonParseException e){
            System.out.println("Cannot parse to Json Document!");
            return null;
        }
        this.S1.clear();
        this.col = 0;
        this.line = 1;
        this.clear();
        return obs.top();
   }

   public JsonObject parse(File fl){
       LinkedStack<JsonObject> obs = new LinkedStack<>();
       obs.push(new JsonObject());
       //FileInputStream f = null;
       LinkedStack<JsonArray> arrays = new LinkedStack<>();
       try (FileInputStream f  = new FileInputStream(fl);
            InputStreamReader ch = new InputStreamReader(f);
       ){
           int c = f.read();
           if((char)c == '{') {
               state = JsParserState.OPENROOT;
               this.S1.push(state);
               col++;
           }
           else
               err((char)c,"{");//root not found!
           JsonString pname = new JsonString("");
           JsonString pflag = new JsonString("");
           while(state != JsParserState.CLOSEROOT && state != JsParserState.ERR && !S1.isEmpty()){//read characters until last '}' or error is found.
               char cur;
               while((cur = (char)getch(f)) == ' ' || cur == '\t' || cur == '\n' || cur == '\r');
               changeState(state,cur,f,obs,arrays,pname,pflag);
           }
           ch.close();
           f.close();
           if(state == JsParserState.ERR)
               return null;
       }catch (FileNotFoundException e){
           System.out.println("File not found. Specify file to read");
           return null;
       } catch (IOException e) {
           System.out.println("File is not available now.");
           return null;
       } catch (JsonParseException e){
           System.out.println("Cannot parse to Json Document!");
           return null;
       }
       this.S1.clear();
       this.col = 0;
       this.line = 1;
       this.clear();
       return obs.top();
   }

   public JsonObject parse(String fileName) {
        LinkedStack<JsonObject> obs = new LinkedStack<>();
        obs.push(new JsonObject());
        LinkedStack<JsonArray> arrays = new LinkedStack<>();
        try (FileInputStream f  = new FileInputStream(new File(fileName).getAbsolutePath());
             InputStreamReader ch = new InputStreamReader(f);
        ){
            int c = f.read();
            if((char)c == '{') {
                state = JsParserState.OPENROOT;
                this.S1.push(state);
                col++;
            }
            else
                err((char)c,"{");//root not found!
            JsonString pname = new JsonString("");
            JsonString pflag = new JsonString("");
            while(state != JsParserState.CLOSEROOT && state != JsParserState.ERR && !S1.isEmpty()){//read characters until last '}' or error is found.
                char cur;
                while((cur = (char)getch(f)) == ' ' || cur == '\t' || cur == '\n' || cur == '\r');
                changeState(state,cur,f,obs,arrays,pname,pflag);
            }
            ch.close();
            f.close();
            if(state == JsParserState.ERR)
                return null;
        }catch (FileNotFoundException e){
            System.out.println("File not found. Specify file to read");
            return null;
        } catch (IOException e) {
            System.out.println("File is not available now.");
            return null;
        } catch (JsonParseException e){
            System.out.println("Cannot parse to Json Document!");
            return null;
        }

        this.S1.clear();
        this.col = 0;
        this.line = 1;
        this.clear();
        return obs.top();
    }

    private void changeState(JsParserState state, char c, InputStream f, LinkedStack<JsonObject> objects, LinkedStack<JsonArray> arrays, JsonString propName, JsonString arflag) throws IOException, JsonParseException {
//        System.out.println("Current state: "+state+ " symbol -> "+c);
//        System.out.println("Stack: "+S1+"\n");
//        System.out.println("At: "+line+":"+col+"\n");
        JsonObject top = objects.top();
        if(state == JsParserState.OPENROOT ){//|| state == JsParserState.OPENBRACE){
            if(c == '}' )//&& state == JsParserState.OPENROOT)
                this.state = JsParserState.CLOSEROOT;
            else if(c != '\"') {
                err(c, "\"");
            }
            else {
                this.state = JsParserState.OPENQP;
            }
        }
        else if(state == JsParserState.OPENQP){
            if(c == '\"') {
                err(c, "Any character except \" and EOF");
                return;
            }
            int l = 1;
            ungetch(c);
            while((c = (char)getFilech(f)) != '\"'){ungetch(c);l++; col++;}
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < buf.length && i < l; i++)
                b.append(buf[i]);
            propName.setVal(b.toString());
            //System.out.println("Property: "+propName);
            b = null;
            clear();
            this.state = JsParserState.CLOSEQP;
        }
        else if(state == JsParserState.CLOSEQP){
            if(c != ':')
                err(c,":");
            else {
                this.state = JsParserState.COLON;
            }
        }
        else if(state == JsParserState.COLON){
            if(c == '[') {
                this.state = JsParserState.OPENARR;
                arrays.push(new JsonArray());
                top.Put(propName.getValue(),arrays.top());
                this.S1.push(this.state);
            }
            else if(c == '{') {
                this.state = JsParserState.START;
                JsonObject inner = new JsonObject();
                top.Put(propName.getValue(),inner);
                objects.push(inner);
                this.S1.push(JsParserState.OPENBRACE);
            }
            else if(c == '\"') {
                this.state = JsParserState.OPENQ;
                ungetch(c);//save first "
            }
            else if(Character.isDigit(c) || c == '-')
                readNum(f,c,propName,top,null);
            else if(c != ',' && c != ':' && c != '}' && c != ']')//read id (feature extension)
                readId(f,c,propName,top,null);
            else
                err(c,"One of [ { \" d+ or true or false");
        }
        else if(state == JsParserState.OPENARR){
            this.S1.push(JsParserState.ARRELEM);
            if(c == '[') {
                this.state = JsParserState.OPENARR;
                JsonArray l = arrays.top();
                arrays.push(new JsonArray());
                l.add(arrays.top());
                this.S1.push(this.state);
            }
            else if(c == '{') {
                this.state = JsParserState.START;
                JsonArray l = arrays.top();
                objects.push(new JsonObject());
                l.add(objects.top());
                this.S1.push(JsParserState.OPENBRACE);
            }
            else if(c == '\"') {
                arflag.setVal("A");
                this.state = JsParserState.OPENQ;
                ungetch(c);//save first "
            }

            else if(c == ']'){
                S1.pop();//end of element
                S1.pop();//end of array.
                arflag.setVal("");
                arrays.pop();
                if(S1.top() == JsParserState.ARRELEM)
                    this.state = JsParserState.CLOSEQ;
                else {
                    S1.push(JsParserState.CLOSEARR);
                    this.state = S1.top();
                }
            }
            else if(Character.isDigit(c))
                readNum(f,c,null,null,arrays.top());//read number [binary,octal,hex formats] and return CLOSEQ
            else if(c != ',' && c != ':' && c != '}' && c != ']')//read id (feature extension)
                readId(f,c,null,top,arrays.top());
            else
                err(c,"One of [ { \" d+ or true or false");
        }

        else if(state == JsParserState.OPENQ){
            //ungetch(c);//save first "
            int l = 0;
            StringBuilder b = new StringBuilder();
            while((c = (char)getFilech(f)) != '\"' || escaped){
                ungetch(c);l++;escaped = false;
            }
            //ungetch(c);//save last "
            for(int i = 0; i < buf.length && i < l; i++)
                b.append(buf[i]);
            String val = b.toString();
            if(l == 0){
                val = "\"\"";
            }
            //System.out.println(propName.getValue()+" : "+val);
            if(!arflag.getValue().equals("A")) {//If we are within in object.
                if(top.getValue().containsKey(propName.getValue()))
                    throw new JsonParseException("Property name \""+propName.getValue()+"\" is already defined in current scope (object)! ",null);
                top.Put(propName.getValue(), new JsonString(val));
            }
            else{//we are withing in array.
                JsonArray array = arrays.top();
                array.add(new JsonString(val));
            }
            b = null;
            clear();
            this.state = JsParserState.CLOSEQ;
        }
        else if(state == JsParserState.ID_READ){
            if(S1.top() == JsParserState.COLON && c != ',' && c != ':' && c != '}' && c != ']' && c != '{' && c != '['){
                this.state = S1.top();
                S1.pop();
                readId(f,c,propName,top,null);
            }
            else if(S1.top() == JsParserState.ARRELEM && c != ',' && c != ':' && c != '}' && c != ']' && c != '{' && c != '['){
                this.state = S1.top();
                S1.pop();
                readId(f,c,null,top,arrays.top());
            }
            else{
                S1.pop();
                this.state = JsParserState.CLOSEQ;
                ungetch(c);
            }
        }
        else if(state == JsParserState.CLOSEQ){
            //System.out.println(propName.getValue()+":"+top.getValue().get(propName.getValue()));
            if(c == ','  && S1.top() == JsParserState.ARRELEM){
                S1.pop();
                arflag.setVal("");
                this.state = S1.top();
            }
            else if(c == ',' && S1.top() == JsParserState.OPENBRACE){
                while((c = (char)getch(f)) == ' ' || c == '\t' || c == '\n' || c == '\r');// skip spaces
                if(c != '\"') {
                    err(c, "\"");
                }
                else
                    this.state = JsParserState.OPENQP;
            }
            else if(c == ']' && S1.top() == JsParserState.ARRELEM){
                S1.pop();//end of element
                S1.pop();//end of array.
                arflag.setVal("");
                arrays.pop();
                if(S1.top() == JsParserState.ARRELEM)
                    this.state = JsParserState.CLOSEQ;
                else {
                    S1.push(JsParserState.CLOSEARR);
                    this.state = S1.top();
                }
            }
            else if(c == '}' && S1.top() == JsParserState.OPENROOT){
                S1.pop();
                this.state = JsParserState.CLOSEROOT;
            }
            else if(c == '}' && S1.top() == JsParserState.OPENBRACE){
                S1.pop();
                S1.push(JsParserState.CLOSEBRACE);
                this.state = S1.top();
            }
            else if(c == ','){
                while((c = (char)getch(f)) == ' ' || c == '\t' || c == '\n' || c == '\r');// skip spaces
                if(c != '\"') {
                    err(c, "\"");
                }
                else
                    this.state = JsParserState.OPENQP;
            }
            else
                err(c,", or ] or } ");
        }
        else if(state == JsParserState.OPENBRACE){
            if(c == '}' && S1.top() == JsParserState.OPENBRACE){
                S1.pop();
                S1.push(JsParserState.CLOSEBRACE);
                this.state = S1.top();
            }
            else if (c == ',') {
                while ((c = (char) getch(f)) == ' ' || c == '\t' || c == '\n' || c == '\r') ;// skip spaces
                    if (c != '\"') {
                        err(c, "\"");
                    } else
                        this.state = JsParserState.OPENQP;
            }
        }
        else if(state == JsParserState.START){
            if(c == '}'){
                S1.pop();
                S1.push(JsParserState.CLOSEBRACE);
                this.state = S1.top();
            }
            else if(c != '\"'){
                err(c,"Expected \"");
            }
            else
                this.state = JsParserState.OPENQP;
        }
        else if(state == JsParserState.CLOSEARR) {//closearr openbrace arrelem
            S1.pop();
            arflag.setVal("");
            if (c == ',') {//array was read as property value. scanning next " for propertyName
                while ((c = (char) getch(f)) == ' ' || c == '\t' || c == '\n' || c == '\r') ;// skip spaces
                if (c != '\"') {
                    err(c, "\"");
                } else
                    this.state = JsParserState.OPENQP;
            }
            //datasets: 125, 559.
            //array was read as last property of inner object.
            //ERROR
            else if(c == '}' && S1.top() == JsParserState.OPENBRACE){//OPENROOT!!
                this.state = JsParserState.CLOSEBRACE;
                this.S1.pop();//close brace [,] openbrace
                if(S1.top() == JsParserState.ARRELEM) {
                    this.S1.push(JsParserState.CLOSEBRACE);
                    ungetch(c);
                }
                else {
                    this.state = JsParserState.CLOSEQ;
                    objects.pop();
                }
            }
        }
        else if(state == JsParserState.CLOSEBRACE){
            S1.pop();
            objects.pop();
            top = objects.top();
            if(c == '}' && S1.top() == JsParserState.OPENROOT)
                this.state = JsParserState.CLOSEROOT;
            else if(c == '}' && S1.top() == JsParserState.OPENBRACE){
                S1.pop();
                S1.push(JsParserState.CLOSEBRACE);
                this.state = S1.top();
            }
            else if(c == '}' && S1.top() == JsParserState.ARRELEM){
                this.state = JsParserState.CLOSEQ;
            }
            else if(c == ',' && S1.top() == JsParserState.ARRELEM){
                S1.pop();
                this.state = S1.top();
            }
            else if(c == ']' && S1.top() == JsParserState.ARRELEM){
                S1.pop();//end of element
                S1.pop();//end of array.
                if(S1.top() == JsParserState.ARRELEM)
                    this.state = JsParserState.CLOSEQ;
                else {
                    if(S1.top() == JsParserState.OPENROOT){
                        char nc = (char) getch(f);
                        ungetch(nc);
                        if(nc == ',')
                            this.state = JsParserState.CLOSEQ;
                        else if(nc == '}')
                            this.state = JsParserState.CLOSEROOT;
                        else
                            this.state = S1.top();
                    }
                    else
                        this.state = S1.top();
                }
            }
            else if(c == ','){
                while ((c = (char) getch(f)) == ' ' || c == '\t' || c == '\n' || c == '\r') ;// skip spaces
                if (c != '\"') {
                    err(c, "\"");
                } else
                    this.state = JsParserState.OPENQP;
            }
        }
    }

    private void err(char f,String msg){
        state = JsParserState.ERR;
        System.out.println("Founded illegal symbol at ("+line+":"+col+"). Expected: "+msg);
        System.out.println("But founded: "+f);
    }

    private int ungetch(char c){
        if(bufp > bsize){
            System.out.println("Error ("+line+":"+col+"). ungetch: too many characters.");
            return 0;
        }
        else{
            buf[bufp++] = c;
            return 1;
        }
    }

    private int getch(InputStream r) throws IOException {
        if(bufp > 0)
            return buf[--bufp];
        else{
            col++;
            char c = (char)r.read();
            if(c == '\n'){
                line++; col = 0;
            }
            else if(c == '\\')
                return getEscaped(r);
            return c;
        }
    }

    private int getEscaped(InputStream r) throws IOException {
        col++;
        char x = (char)r.read();
        escaped = true;
        switch (x){
            case 't':{
                col++;
                return '\t';
            }
            case 'r':{
                col++;
                return '\r';
            }
            case 'n':{
                col++;
                return '\n';
            }
            case 'f':{
                col++;
                return '\f';
            }
            case 'b':{
                col++;
                return '\b';
            }
            case '\'':{
                col++;
                return '\'';
            }
            case '\"':{
                col++;
                return '\"';
            }
            case '\\':{
                col++;
                return '\\';
            }
            case 'u':{
                col++;
                int i = 0;
                char[] hcode = new char[4];
                while(i < 4 && ( ((x = (char) r.read()) >= '0' && x <='9') || (x >= 'A' && x <= 'F') || (x >= 'a' && x <= 'f') )){
                    hcode[i] = x;
                    i++;
                }
                if(i < 4) {
                    err(x, "hexDigit: one of [0-9] or [A-Fa-f]");
                    return 0;
                }
                int code = (int)ProcessExp.parse(new String(hcode),null,'N',16,1);
                return code;
            }
            default:{
                ungetch(x);
                return '\\';
            }
        }
    }

    private int getFilech(InputStream r) throws IOException {
        char c = (char)r.read();
        if(c == '\n'){
            line += 1;
            col = 0;
        }
        else if(c == '\\')
            return getEscaped(r);
        else{
            col += 1;
        }
        return c;
    }

    private void clear(){
       EOL = 0;
       bufp = 0;
       this.buf = null;
       this.buf = new char[bsize];
    }

    //Read named token or name of the object (ob).
    private void readId(InputStream f, char c,JsonString propName, JsonObject ob, JsonArray arr) throws IOException {
        StringBuilder b = new StringBuilder();
        int i = 0;
        b.append(c);
        while(i < 2147483647){
            c = (char)getch(f);
            if(c == ',' || c == '\t' || c =='\n' || c == ' ' || c == '}' || c == ']' || c == '\r' || c == '{' || c == '[')
                break;
            b.append(c);
            i++;
        }
        String v = b.toString();

        //IF PROPERTY IS ALREADY INIT (IN CASE OF MUTIPLE IDS)
        JsonElement old = null;
        if(ob != null && propName != null && (old = ob.getProperty(propName.getValue())) != null){
            try{
                JsonString ov = (JsonString)old;
                JsonElement l =  ob.getElement(v);
                JsonString appendv = null;
                if(l == null)
                    appendv = new JsonString(v);
                else
                    appendv = (JsonString)l;
                JsonString nv = new JsonString(ov.getValue() + appendv.getValue());
                ob.Put(propName.getValue(),nv);
            }
            catch (ClassCastException ex){
                System.out.println("Property \""+propName.getValue()+"\" has already value.");
                System.out.println("All ids MUST REFER TO STRING values!");
                throw new JsonParseException("Cannot use multiple ids with values of different types!",null);
            }
        }
        else if(v.equals("true")){
            addLiteral(ob,propName,arr,new JsonBoolean('t'));
        }
        else if(v.equals("false")){
            addLiteral(ob,propName,arr,new JsonBoolean('f'));
        }
        else if(v.equals("null")){
            addLiteral(ob,propName,arr,new JsonNull());
        }
        else{
            JsonElement l =  ob.getElement(v);
            //if id is not a propName THEN treat it as a STRING literal
            addLiteral(ob,propName,arr,(l == null) ? new JsonString(v) : l);
        }

        //SINGLE ID WAS READ.
        //this.state = JsParserState.CLOSEQ;

        //ID WAS READ. TRY(READ NEXT ID  OR  COMMA (,))
        S1.push(this.state);//save prev state.
        this.state = JsParserState.ID_READ;

        ungetch(c);
    }

    //read numeric literal [Real: hex,binary,octal; Integer: hex,binary,octal;]
    private void readNum(InputStream f, char c,JsonString propName, JsonObject ob, JsonArray arr) throws IOException {
        int realf = 0, zerof = 1,ex = 0;
        Pair<Integer,Integer> nflags = new Pair<>(0,0);
        StringBuilder b = new StringBuilder();
        int sign = 1;
        if(c == '-'){
            sign = -1;
            c = (char)getch(f);
        }
        if(c == '0') {
            ungetch(c);//save 0.
            if((c = (char)getFilech(f)) == 'x' || c == 'X'){//0x => hex-decimal preffix.
                getch(f);//preffix was read.
                nflags.setV1(2);
                zerof = 0;
                c = (char)readNumericHex(f,c,nflags,b);
                int che = nflags.getV2();
                if(c == '.'){
                    b.append(c);
                    nflags.setV1(nflags.getV1() + 1);//i++
                    char c1 = (char)getch(f);
                    ungetch(c1);
                    if((c1 >= '0' && c1 <= '9') || (c1 >= 'A' && c1 <= 'F') || (c1 >= 'a' && c1 <= 'f')){
                        c = (char)readNumericHex(f,c,nflags,b);
                        nflags.setV2(1);
                        realf = 1;
                    }
                    else if(nflags.getV2() > 0){//after dot there is no any hex digit.
                        //if non-exponent
                        err(c1,"0-9 or A-F or a-f");
                        return;
                    }
                }
                if(nflags.getV2() == 0){
                    err(c,"0-9 or A-F or a-f");
                    return;
                }
                if(c == 'P' || c == 'p' || c == 'H' || c == 'h'){
                    String n = b.toString();
                    char exp = c;
                    b = null;
                    b = new StringBuilder();
                    nflags.setV1(0);
                    nflags.setV2(0);
                    c = (char)readExponent(f,c,nflags,b);
                    if(nflags.getV2() == 0){//exponent was read?
                        err(c,"0-9");
                        return;
                    }
                    String e = b.toString();
                    b = null;
                    b = new StringBuilder();
                    b.append(ProcessExp.parse(n,e,exp,16,sign));
                    ex = 1;
                }
                else{
                    String n = b.toString();
                    b = null;
                    b = new StringBuilder();
                    b.append(ProcessExp.parse(n,null,'O',16,sign));//without exponent. parse it to decimal format (valid JSON)
                }
                if(c == 'L' || c == 'l' || c == 'S' || c == 's'){
                    String num = b.toString();
                    if((c == 'L' || c == 'l')) {
                        if(realf == 1  || num.contains("."))
                            num = num.substring(0,num.indexOf('.'));
                        addLiteral(ob,propName,arr,new JsonNumber(Long.parseLong(num)));
                        this.state = JsParserState.CLOSEQ;
                    }
                    else{
                        addLiteral(ob,propName,arr,new JsonRealNumber(Float.parseFloat(num)));
                        this.state = JsParserState.CLOSEQ;
                    }
                    return;
                }
                else{
                    addLiteral(ob,propName,arr,new JsonRealNumber(Double.parseDouble(b.toString())));
                    this.state = JsParserState.CLOSEQ;
                  //  if(c == ',' || c == '\t' || c =='\n' || c == ' ')
                        ungetch(c);
                 //   else if(c != 'D' && c != 'd')
                  //      err(c,"Expected currect suffix [DdSsLl]");
                    return;
                }
            }
            else if(c == 'B' || c == 'b'){ //0b => binary preffix.
                getch(f);//preffix was read.
                nflags.setV1(2);
                zerof = 0;
                c = (char)readNumericBin(f,c,nflags,b);
                if(c == '.'){//0b.2
                    b.append(c);
                    nflags.setV1(nflags.getV1() + 1);//i++
                    char c1 = (char)getch(f);
                    ungetch(c1);
                    if((c1 >= '0' && c1 <= '1')){
                        c = (char)readNumericBin(f,c,nflags,b);
                        nflags.setV2(1);
                        realf = 1;
                    }
                    else if(nflags.getV2() > 0){//after dot there is no any hex digit.
                        err(c1,"0,1");
                        return;
                    }
                }
                if(nflags.getV2() == 0){
                    err(c,"0,1");
                    return;
                }
                if(c == 'P' || c == 'p' || c == 'E' || c == 'e'){
                    String n = b.toString();
                    char exp = c;
                    b = null;
                    b = new StringBuilder();
                    nflags.setV1(0);
                    nflags.setV2(0);
                    c = (char)readExponent(f,c,nflags,b);
                    if(nflags.getV2() == 0){//exponent was read?
                        err(c,"0-9");
                        return;
                    }
                    String e = b.toString();
                    b = null;
                    b = new StringBuilder();
                    b.append(ProcessExp.parse(n,e,exp,2,sign));
                    ex = 1;
                }
                else{
                    String n = b.toString();
                    b = null;
                    b = new StringBuilder();
                    b.append(ProcessExp.parse(n,null,'O',2,sign));//without exponent. parse it to decimal format (valid JSON)
                }
                if(c == 'L' || c == 'l' || c == 'F' || c == 'f'){
                    String num = b.toString();
                    if((c == 'L' || c == 'l')) {
                        if(realf == 1  || num.contains("."))
                            num = num.substring(0,num.indexOf('.'));
                        addLiteral(ob,propName,arr,new JsonNumber(Long.parseLong(num)));
                        this.state = JsParserState.CLOSEQ;
                    }
                    else{
                        addLiteral(ob,propName,arr,new JsonRealNumber(Float.parseFloat(num)));
                        this.state = JsParserState.CLOSEQ;
                    }
                    return;
                }
                else{
                    addLiteral(ob,propName,arr,new JsonRealNumber(Double.parseDouble(b.toString())));
                    this.state = JsParserState.CLOSEQ;
                 //   if(c == ',' || c == '\t' || c =='\n' || c == ' ')
                        ungetch(c);
                //    else if(c != 'D' && c != 'd')
                 //       err(c,"Expected currect suffix [DdFfLl]");
                    return;
                }
            }
            else if(c == 'C' || c == 'c'){
                getch(f);//preffix was read '0c'
                nflags.setV1(2);
                zerof = 0;
                c = (char)readNumericOct(f,c,nflags,b);
                if(c == '.'){
                    b.append(c);
                    nflags.setV1(nflags.getV1() + 1);//i++
                    char c1 = (char)getch(f);
                    ungetch(c1);
                    if((c1 >= '0' && c1 <= '7')){
                        c = (char)readNumericOct(f,c,nflags,b);
                        nflags.setV2(1);
                        realf = 1;
                    }
                    else if(nflags.getV2() > 0){//after dot there is no any hex digit.
                        err(c1,"0-7");
                        return;
                    }
                }
                if(nflags.getV2() == 0){
                    err(c,"0-7");
                    return;
                }
                if(c == 'P' || c == 'p' || c == 'E' || c == 'e'){
                    String n = b.toString();
                    char exp = c;
                    b = null;
                    b = new StringBuilder();
                    nflags.setV1(0);
                    nflags.setV2(0);
                    c = (char)readExponent(f,c,nflags,b);
                    if(nflags.getV2() == 0){//exponent was read?
                        err(c,"0-9");
                        return;
                    }
                    String e = b.toString();
                    b = null;
                    b = new StringBuilder();
                    b.append(ProcessExp.parse(n,e,exp,8,sign));
                    ex = 1;
                }
                else{
                    String n = b.toString();
                    b = null;
                    b = new StringBuilder();
                    b.append(ProcessExp.parse(n,null,'O',8,sign));//without exponent. parse it to decimal format (valid JSON)
                }
                if(c == 'L' || c == 'l' || c == 'F' || c == 'f'){
                    String num = b.toString();
                    if((c == 'L' || c == 'l')) {
                        if(realf == 1  || num.contains("."))
                            num = num.substring(0,num.indexOf('.'));
                        addLiteral(ob,propName,arr,new JsonNumber(Long.parseLong(num)));
                        this.state = JsParserState.CLOSEQ;
                    }
                    else{
                        addLiteral(ob,propName,arr,new JsonRealNumber(Float.parseFloat(num)));
                        this.state = JsParserState.CLOSEQ;
                    }
                    return;
                }
                else{
                    addLiteral(ob,propName,arr,new JsonRealNumber(Double.parseDouble(b.toString())));
                    this.state = JsParserState.CLOSEQ;
                   // if(c == ',' || c == '\t' || c =='\n' || c == ' ')
                        ungetch(c);
                    //else if(c != 'D' && c != 'd')
                    //    err(c,"Expected currect suffix [DdFfLl]");
                    return;
                }
            }
            getch(f);//free zero.
            b.append('0');
            ungetch(c);
            zerof = 1;
        }
        else{
            if((c >= '1' && c <= '9') || c == '.') {
                b.append(c);
                ex = 1;
            }
            else {
                err(c, "Expected 0-9");
                return;
            }
        }
        if(c  != '.'){//decimal numeric literal.
            c = (char)readNumeric(f,c,nflags,b);
            if(c == '.'){
                b.append('.');
                nflags.setV1(nflags.getV1() + 1);
                char c1 = (char)getch(f);
                ungetch(c1);
                if((c1 >= '0' && c1 <= '9')){
                    c = (char)readNumeric(f,c,nflags,b);
                    nflags.setV2(1);
                    realf = 1;
                }
                else if(nflags.getV2() > 0){//after dot there is no any hex digit.
                    err(c1,"0-9");
                    return;
                }
                else if(ex == 1){
                    err(c1,"0-9");
                    return;
                }
            }
            //exponent part
            if(c == 'P' || c == 'p' || c == 'E' || c == 'e'){
                String n = b.toString();
                char exp = c;
                b = null;
                b = new StringBuilder();
                nflags.setV1(0);
                nflags.setV2(0);
                c = (char)readExponent(f,c,nflags,b);
                if(nflags.getV2() == 0){//exponent was read?
                    err(c,"0-9");
                    return;
                }
                String e = b.toString();
                b = null;
                b = new StringBuilder();
                b.append(ProcessExp.parse(n,e,exp,10,sign));
                ex = 1;
            }
            else{
                String n = b.toString();
                b = null;
                b = new StringBuilder();
                b.append(ProcessExp.parse(n,null,'O',10,sign));//without exponent. parse it to decimal format (valid JSON)
            }
            if(c == 'L' || c == 'l' || c == 'F' || c == 'f'){
                String num = b.toString();
                if((c == 'L' || c == 'l')) {
                    if(realf == 1 || num.contains("."))
                        num = num.substring(0,num.indexOf('.'));
                    addLiteral(ob,propName,arr,new JsonNumber(Long.parseLong(num)));
                    this.state = JsParserState.CLOSEQ;
                }
                else{
                    addLiteral(ob,propName,arr,new JsonRealNumber(Float.parseFloat(num)));
                    this.state = JsParserState.CLOSEQ;
                }
                return;
            }
            else {
                addLiteral(ob,propName,arr,new JsonRealNumber(Double.parseDouble(b.toString())));
                this.state = JsParserState.CLOSEQ;
               // if(c == ',' || c == '\t' || c =='\n' || c == ' ' || c == ']' || c == '}')
                    ungetch(c);
                //else if(c != 'D' && c != 'd')
                //    err(c,"Expected currect suffix [DdFfLl]");
                return;
            }
        }
        else {//dot token -> decimal number without integral part [i.e .123 or .5e3 or .95]
            if(zerof == 1) {
                getch(f);//remove '.' from buffer if it was 0.
            }
            b.append(c);
            char c1 = (char)getch(f);
            ungetch(c1);
            if((c1 >= '0' && c1 <= '9')){
                c = (char)readNumeric(f,c,nflags,b);
                nflags.setV2(1);
                realf = 1;
            }
            else if(zerof == 1){//after dot there is no any hex digit.
                err(c1,"0-9");
                return;
            }
            //exponent part
            if(c == 'P' || c == 'p' || c == 'E' || c == 'e'){
                String n = b.toString();
                char exp = c;
                b = null;
                b = new StringBuilder();
                nflags.setV1(0);
                nflags.setV2(0);
                c = (char)readExponent(f,c,nflags,b);
                if(nflags.getV2() == 0){//exponent was read?
                    err(c,"0-9");
                    return;
                }
                String e = b.toString();
                b = null;
                b = new StringBuilder();
                b.append(ProcessExp.parse(n,e,exp,10,sign));
                ex = 1;
            }
            else{
                String n = b.toString();
                b = null;
                b = new StringBuilder();
                b.append(ProcessExp.parse(n,null,'O',10,sign));//without exponent. parse it to decimal format (valid JSON)
            }
            if(c == 'L' || c == 'l' || c == 'F' || c == 'f'){
                String num = b.toString();
                if((c == 'L' || c == 'l')) {
                    if(realf == 1  || num.contains("."))
                        num = num.substring(0,num.indexOf('.'));
                    addLiteral(ob,propName,arr,new JsonNumber(Long.parseLong(num)));
                    this.state = JsParserState.CLOSEQ;
                }
                else{
                    addLiteral(ob,propName,arr,new JsonRealNumber(Float.parseFloat(num)));
                    this.state = JsParserState.CLOSEQ;
                }
                return;
            }
            else{
                addLiteral(ob,propName,arr,new JsonRealNumber(Double.parseDouble(b.toString())));
                this.state = JsParserState.CLOSEQ;
                //if(c == ',' || c == '\t' || c =='\n' || c == ' ')
                    ungetch(c);
                //else if(c != 'D' && c != 'd')
                //    err(c,"Expected currect suffix [DdFfLl]");
                return;
            }
        }
    }

    private int readNumeric(InputStream f, char c, Pair<Integer,Integer> flags, StringBuilder builder) throws IOException {
        int cur = flags.getV1();
        int nflag = 0;
        while(cur < 2147483647){
            int flag_ = 1;
            while(flag_ > 0){
                while( ((c = (char)getch(f)) >= '0' && c <= '9') ){
                    builder.append(c);
                    cur += 1;
                    nflag = 1;
                }
                if(c == '_'){
                    while(c == '_') c = (char)getFilech(f);
                    ungetch(c);//after _ it is digit or other symbol.
                }
                else{
                    flag_ = 0;
                }
            }
            flags.setV1(cur);
            flags.setV2(nflag);
            return c;
        }
        flags.setV1(cur);
        flags.setV2(0);
        //this.state = JsParserState.ERR;
        return -1;
    }

    private int readNumericHex(InputStream f, char c, Pair<Integer,Integer> flags, StringBuilder builder) throws IOException {
        int cur = flags.getV1();
        int nflag = 0;
        while(cur < 2147483647){
            int flag_ = 1;
            while(flag_ > 0){
                while( ((c = (char)getch(f)) >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') ){
                    builder.append(c);
                    cur += 1;
                    nflag = 1;
                }
                if(c == '_'){
                    while(c == '_') c = (char)getFilech(f);
                    ungetch(c);//after _ it is digit or other symbol.
                }
                else{
                    flag_ = 0;
                }
            }
            flags.setV1(cur);
            flags.setV2(nflag);
            return c;
        }
        flags.setV2(0);
        //this.state = JsParserState.ERR;
        return -1;
    }

    private int readNumericOct(InputStream f, char c, Pair<Integer,Integer> flags, StringBuilder builder) throws IOException {
        int cur = flags.getV1();
        int nflag = 0;
        while(cur < 2147483647){
            int flag_ = 1;
            while(flag_ > 0){
                while( ((c = (char)getch(f)) >= '0' && c <= '7')){
                    builder.append(c);
                    cur += 1;
                    nflag = 1;
                }
                if(c == '_'){
                    while(c == '_') c = (char)getFilech(f);
                    ungetch(c);//after _ it is digit or other symbol.
                }
                else{
                    flag_ = 0;
                }
            }
            flags.setV1(cur);
            flags.setV2(nflag);
            return c;
        }
        flags.setV2(0);
        //this.state = JsParserState.ERR;
        return -1;
    }

    private int readNumericBin(InputStream f, char c, Pair<Integer,Integer> flags, StringBuilder builder) throws IOException {
        int cur = flags.getV1();
        int nflag = 0;
        while(cur < 2147483647){
            int flag_ = 1;
            while(flag_ > 0){
                while( ((c = (char)getch(f)) >= '0' && c <= '1')){
                    builder.append(c);
                    cur += 1;
                    nflag = 1;
                }
                if(c == '_'){
                    while(c == '_') c = (char)getFilech(f);
                    ungetch(c);//after _ it is digit or other symbol.
                }
                else{
                    flag_ = 0;
                }
            }
            flags.setV1(cur);
            flags.setV2(nflag);
            return c;
        }
        flags.setV2(0);
        //this.state = JsParserState.ERR;
        return -1;
    }

    private int readExponent(InputStream f, char c, Pair<Integer,Integer> flags, StringBuilder builder) throws IOException {
        int cur = flags.getV1();
        int err = 0;
        while(cur < 2147483647 && err < 2){
            if((c = (char)getFilech(f)) == '+' || c == '-'){//read sign of the exponent.
                c = c == '+' ? '+' : '-';
                builder.append(c);
                cur += 1;
                err += 1;
            }
            else
                ungetch(c);
            flags.setV1(cur);
            c = (char)readNumeric(f, c,flags,builder);
            return c;//or suffix_type or illegal symbol
        }
        flags.setV2(0);
        return -1;
    }

    private <T> void addLiteral(JsonObject ob, JsonString propName, JsonArray arr,JsonElement<T> el){
        if(propName == null || ob == null){//property name was not specified => it is an array element.
            arr.add(el);
        }
        else
            ob.Put(propName.getValue(),el);
    }
}
