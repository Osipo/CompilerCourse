package ru.osipov.labs.lab1.structures.graphs;


import ru.osipov.labs.lab1.structures.lists.LinkedStack;

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
