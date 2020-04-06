package ru.osipov.labs.lab1.structures.graphs;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private String name;
    private List<Edge> edges;
    private boolean isStart;//redundant
    private boolean isFinish;
    private boolean isDead;


    public Vertex(){
        this.name = "";
        this.isStart = false;
        this.isFinish = false;
        this.isDead = false;
        this.edges = new ArrayList<>();
    }

    public void setStart(boolean e){
        this.isStart = e;
    }

    public void setFinish(boolean e) {
        this.isFinish = e;
    }

    public void setName(String n){
        this.name = n;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public void addEdge(Edge e){
        if(!edges.contains(e))
            edges.add(e);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public boolean isStart() {
        return isStart;
    }

    public String getName(){
        return name;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Vertex) {
            Vertex b = (Vertex) obj;
            return isStart == b.isStart && isFinish == b.isFinish && name.equals(b.name);
        }
        return false;
    }

    public boolean isDead() {
        return isDead;
    }

    @Override
    public String toString(){
        return this.name;
    }
}
