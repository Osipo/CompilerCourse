package ru.osipov.labs.lab1.structures.graphs;


import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import ru.osipov.labs.lab1.structures.lists.LinkedStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
//            if(v.isDead()){
//                sb.append(",color=\"red\", shape=\"box\"");
//            }
            sb.append("]");
            sb.append(";\n");
        }
        for(Edge e : edges){
            String a = e.getSource().getName();
            String b = e.getTarget().getName();
            String l = ((int)e.getTag()) == 1 ? "empty" : e.getTag()+"";
            sb.append(a).append(" -> ").append(b);
            sb.append("[ label=\"").append(l).append("\"");
            sb.append("];\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public void getImagefromStr(String path, String fname) throws IOException {
        Graphviz.fromString(toDotStr(fname)).render(Format.PNG).toFile(new File(path+fname));
    }

    public void addNode(Vertex v){ //ERR
        if(!nodes.contains(v)) {
            qnodes++;
            if(v.getName() == null || v.getName().equals(""))
                v.setName(qnodes+"");
            LinkedStack<Vertex> others = new LinkedStack<>();
            nodes.add(v);
            for (Edge e : v.getEdges()) {
                if(e.getSource().equals(v) && e.getTarget().equals(v))
                    continue;//skip loops.
                if(e.getSource().equals(v))//from v
                    others.push(e.getTarget());
                else if(e.getTarget().equals(v))
                    others.push(e.getSource());

                _addEdgeFrom(e);
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
