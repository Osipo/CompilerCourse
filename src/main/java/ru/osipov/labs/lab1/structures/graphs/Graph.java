package ru.osipov.labs.lab1.structures.graphs;


import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Graph {
    protected List<Vertex> nodes;
    protected List<Edge> edges;
    protected int qnodes;

    public Graph(){
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.qnodes = 0;
    }

    public String toDotStr(String name){
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(name).append("{\n");
        for(Vertex v : nodes){
            sb.append(v.getName());
            sb.append("[ label=\"").append(v.getName()).append("\"");
            if(v.isStart()){
                if(v.isFinish()){
                    sb.append(",color=\"blue\", shape=\"doublecircle\"");
                    sb.append("];\n");
                    continue;
                }
                else
                    sb.append(",color=\"blue\", shape=\"invtriangle\"];\n");
                continue;
            }
            if(v.isFinish()){
                sb.append(",color=\"black\", shape=\"doublecircle\"];\n");
                continue;
            }
            sb.append(",shape=\"circle\"");
            if(v.isDead()){
                sb.append(",color=\"red\"");
            }
            sb.append("]");
            sb.append(";\n");
        }
        boolean sh = false;
        for(Edge e : edges){
            String a = e.getSource().getName();
            String b = e.getTarget().getName();
            String l;
            if((int)e.getTag() == 0)
                l = "any";
            else
                l = ((int)e.getTag()) == 1 ? "empty" : e.getTag() == '\"' ? "\\\"" : e.getTag()+"";
            if(e.getTag() == '\\')
                l = "\\\\";
            sb.append(a).append(" -> ").append(b);
            sb.append("[ label=\"").append(l).append("\"");
            sb.append("];\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public void toDotFile(String fname){
        File f = new File(fname);
        if(f.lastModified() != 0){
            System.out.println("Cannot write to existing file!");
            return;
        }
        try (FileWriter fw = new FileWriter(f,true);){
            fw.write("digraph G {\n");
            for(Vertex v : nodes){
                fw.write(v.getName());
                fw.write("[ label=\"");
                fw.write(v.getName()+"\"");
                if(v.isStart()){
                    if(v.isFinish()){
                        fw.write(",color=\"blue\", shape=\"doublecircle\"");
                        fw.write("];\n");
                        continue;
                    }
                    else
                        fw.write(",color=\"blue\", shape=\"invtriangle\"];\n");
                    continue;
                }
                if(v.isFinish()){
                    fw.write(",color=\"black\", shape=\"doublecircle\"];\n");
                    continue;
                }
                fw.write(",shape=\"circle\"");
                if(v.isDead()){
                    fw.write(",color=\"red\"");
                }
                fw.write(']');
                fw.write(";\n");
            }
            for(Edge e : edges){
                String a = e.getSource().getName();
                String b = e.getTarget().getName();
                String l = ((int)e.getTag()) == 1 ? "empty" : e.getTag()+"";
                fw.write(a);fw.write(" -> ");fw.write(b);
                fw.write("[ label=\"");
                fw.write(l+"\"");
                fw.write("];\n");
            }
            fw.write("}");
        }
        catch (FileNotFoundException e){
            System.out.println("Cannot open file to write.");
        } catch (IOException e) {
            System.out.println("Cannot write to file");
        }
    }

    public void getImagefromStr(String path, String fname) throws IOException {
        Graphviz.fromString(toDotStr(fname)).render(Format.PNG).toFile(new File(path+fname));
    }

    public void getImageFromFile(String fname,String fname2) throws IOException {
        Graphviz.fromFile(new File(fname)).render(Format.PNG).toFile(new File(fname2));
    }

    public void addNode(Vertex v){ //ERR
        if(!nodes.contains(v)) {
            qnodes++;
            if(v.getName() == null || v.getName().equals(""))
                v.setName(qnodes+"");
            LinkedStack<Vertex> others = new LinkedStack<>();
            nodes.add(v);
            for (Edge e : v.getEdges()) {
                _addEdgeFrom(e);
                if(e.getSource().equals(v) && e.getTarget().equals(v))
                    continue;//skip loops.
                if(e.getSource().equals(v))//from v
                    others.push(e.getTarget());
                else if(e.getTarget().equals(v))
                    others.push(e.getSource());
            }
            while(!others.isEmpty()){
                Vertex t = others.top();
                others.pop();
                List<Edge> ne = t.getEdges();//.stream().filter(edge -> !edges.contains(edge)).collect(Collectors.toList());
                for(Edge e : ne){
                    if(e.getSource().equals(t) && !nodes.contains(e.getTarget()))
                        others.push(e.getTarget());
                    else if(e.getTarget().equals(t) && !nodes.contains(e.getSource()))
                        others.push(e.getSource());
                    _addEdgeFrom(e);
                }
                if(!nodes.contains(t)){
                    qnodes++;
                    if(t.getName() == null || t.getName().equals(""))
                        t.setName(qnodes+"");
                    nodes.add(t);
                }
            }
        }
    }

    private void _addEdgeFrom(Edge e){
        if(!edges.contains(e)) {
            edges.add(e);
        }
    }

    public List<Vertex> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public int getNodesCount(){
        return qnodes;
    }

    public void disconnectVertexByEdge(Edge e, Vertex v,Vertex t){
        if(edges.contains(e) && e.getSource().equals(v) && e.getTarget().equals(t)){
            e.disconnectNodes();
        }
    }
}
