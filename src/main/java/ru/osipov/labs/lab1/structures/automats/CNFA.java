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

    public void setFinished(List<Vertex> v){
        comboF.addAll(v);
    }

    public Set<Vertex> getFinished(){
        return comboF;
    }
}
