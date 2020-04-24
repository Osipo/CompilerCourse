package ru.osipov.labs.lab1.structures.automats;

import ru.osipov.labs.lab1.structures.graphs.Vertex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//NFA that contains more than one final state.
public class CNFA extends NFA{
    private HashSet<Vertex> comboF;
    public CNFA(){
        this.comboF = new HashSet<>();
    }

    public boolean hasMultipleFinish(){
        return comboF.size() > 1;
    }

    public void setComboStart(Vertex v){
        addNode(v);
        v.setStart(true);
        this.start = v;
        for(Vertex vd : nodes){
            //System.out.println(vd+" : "+vd.isFinish());
            if(vd.isFinish()) {
                comboF.add(vd);
                break;
            }
        }
    }

    public void addToFinished(Vertex v){
        if(v.isFinish())
            comboF.add(v);
    }

    public void setFinished(List<Vertex> v){
        comboF.addAll(v);
    }

    public Set<Vertex> getFinished(){
        return comboF;
    }

    public void setFinish(Vertex v){
        this.finish = v;
        comboF.add(v);
    }
}
