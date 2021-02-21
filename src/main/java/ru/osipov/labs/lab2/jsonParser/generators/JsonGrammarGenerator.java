package ru.osipov.labs.lab2.jsonParser.generators;

import ru.osipov.labs.lab2.jsonParser.jsElements.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JsonGrammarGenerator {

    //Makes json serialization.
    public void toJsonFile(JsonObject ob, String fname) throws IOException {
        File f = new File(fname);
        if(f.lastModified() != 0){
            System.out.println("Cannot write to existing file!");
            return;
        }
        try (FileWriter fw = new FileWriter(f,true);){
            fw.write("{\n\t");

            //Writes all terminal symbols.
            fw.write("\"terms\": {\n\t\t");
            fw.write("\"string\": \"\\\"((\\\\_)|_)*\\\"\",\n\t\t");
            fw.write("\"num\": \"(@-|@+|empty)[0-9]+\",\n\t\t");
            fw.write("\"realNum\": \"(@-|@+|empty)[0-9]+.[0-9]+(((E|e|P|p)(-|empty)[0-9]+)|empty)\",\n\t\t");
            fw.write("\"{\": \"{\",\n\t\t");
            fw.write("\"}\": \"}\",\n\t\t");
            fw.write("\"[\": \"[\",\n\t\t");
            fw.write("\"]\": \"]\",\n\t\t");
            fw.write("\",\": \",\",\n\t\t");
            fw.write("\":\": \":\",\n\t\t");
            fw.write("\"empty\": null");
            fw.write("\n\t},\n\t");//END OF terms of Grammar

            //Write start symbol of Grammar and finish document.
            fw.write("\"start\": ");
            fw.write("\"S\"");
            fw.write("\n}");
        }
        catch (FileNotFoundException e){
            System.out.println("Cannot open file to write.");
        } catch (IOException e) {
            System.out.println("Cannot write to file");
        }
    }
}
