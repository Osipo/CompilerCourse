package ru.osipov.labs.lab1.structures.graphs;

public class Edge {
    private char tag;
    private Vertex source;
    private Vertex target;

    public Edge(char t){
        this(null,null,t);
    }

    public Edge(Vertex a, Vertex b,char t){
        this.tag = t;
        this.source = a;
        this.target = b;
        this.source.addEdge(this);
        this.target.addEdge(this);
    }

    public void setSource(Vertex source) {
        this.source = source;
        source.addEdge(this);
    }

    public void setTarget(Vertex target) {
        this.target = target;
        target.addEdge(this);
    }

    public void setTag(char tag) {
        this.tag = tag;
    }

    public void connect(Vertex a, Vertex b){
        if (!(this.source==null&&this.target==null)){
            disconnectNodes();
        }
        this.source = a;
        this.target = b;
        this.source.addEdge(this);
        this.target.addEdge(this);
    }

    public void disconnectNodes(){
        this.source.getEdges().remove(this);
        this.target.getEdges().remove(this);
        this.source = null;
        this.target = null;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    public char getTag() {
        return tag;
    }
}
