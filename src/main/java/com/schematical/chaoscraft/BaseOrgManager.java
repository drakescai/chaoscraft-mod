package com.schematical.chaoscraft;

import com.schematical.chaoscraft.entities.OrgEntity;
import com.schematical.chaoscraft.tickables.OrgPositionManager;
import com.schematical.chaoscraft.tickables.iChaosOrgTickable;
import com.schematical.chaosnet.model.Organism;
import net.minecraft.entity.Entity;

import java.util.ArrayList;

public abstract class BaseOrgManager {
    protected Organism organism;
    protected OrgEntity orgEntity;
    private ArrayList<iChaosOrgTickable> tickables = new ArrayList<iChaosOrgTickable>();

    public void attachOrganism(Organism organism){
        this.organism = organism;
    }
    public void attachOrgEntity(OrgEntity orgEntity){
        this.orgEntity = orgEntity;

    }

    public  OrgEntity getEntity(){
        return orgEntity;
    };
    public String getCCNamespace() {
        if(organism == null){
            return null;
        }
        return organism.getNamespace();
    }


    public Organism getOrganism() {
        return organism;
    }

    public void attatchTickable(iChaosOrgTickable tickable){
        tickables.add(tickable);
    }
    public void fireTickables() {
        for (iChaosOrgTickable tickable : tickables) {
            tickable.Tick(this);
        }
    }

    public iChaosOrgTickable getTickable(Class<?> t){
        for (iChaosOrgTickable tickable : tickables) {
            if(t.isInstance(tickable)){
                return tickable;
            }
        }
        return null;
    }
}
