package com.schematical.chaoscraft.ai;


import com.schematical.chaoscraft.ai.activators.iActivator;
import com.schematical.chaosnet.model.ChaosNetException;
import net.minecraft.entity.LivingEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user1a on 12/8/18.
 */
public abstract class NeuronBase extends InnovationBase {

    public String id;
    protected LivingEntity debugEntity = null;
    public NeuralNet nNet;
    private iActivator activator;
    private float _lastValue;
    private float _currentValue;
    private boolean hasBeenEvaluated = false;

    public List<NeuronDep> dependencies = new ArrayList<NeuronDep>();
    public abstract String _base_type();
    public void setCurrentValue(float currentValue){
        _currentValue = currentValue;
    }
    public float getCurrentValue(){
        return _currentValue;
    }
    public NeuralNet getNNet(){
        return nNet;
    }
    public void setDebugEntity(LivingEntity debugEntity){
        this.debugEntity = debugEntity;
    }
    public LivingEntity getEntity(){
        if(debugEntity != null){
            return debugEntity;
        }
        return nNet.entity;
    }
    public void parseData(JSONObject jsonObject){
        if(this.id != null){
            throw new Error("Neuron has already parsedData");
        }
        this.id = (String) jsonObject.get("id");
        JSONArray jsonDependencies = (JSONArray) jsonObject.get("dependencies");
        Iterator<JSONObject> iterator = jsonDependencies.iterator();
        while (iterator.hasNext()) {
            JSONObject neuronDepJSON = iterator.next();
            NeuronDep neuronDep = new NeuronDep();
            neuronDep.parseData(neuronDepJSON);
            if(neuronDep.depNeuronId.equals(id)){
                throw new ChaosNetException("Invalid `neuronDep` - NeuronDep on self: " + id);
            }
            this.dependencies.add(neuronDep);
        }

        Object type = jsonObject.get("activator");


        if(type == null){
            type = "Sigmoid";
           // throw new Error("Invalid Neuron. Missing `activator` - Org.namepace: " + nNet.entity.getCCNamespace() + " - Neuron: " + jsonObject.toJSONString());
        }

        String clsName = type.toString() + "Activator";  // use fully qualified name

        String fullClassName = "com.schematical.chaoscraft.ai.activators." + clsName;
        //ChaosCraft.logger.info("Full Class name: " + fullClassName);
        Class cls = null;
        try {
            cls = Class.forName(fullClassName);
            activator = (iActivator) cls.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ChaosNetException(e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ChaosNetException(e.getMessage());
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ChaosNetException(e.getMessage());
        }


    }
    public void reset(){
        hasBeenEvaluated = false;
        _lastValue = _currentValue;
        _currentValue = -1;
    }
    public float evaluate(){
        float newValue = -1;
        if(hasBeenEvaluated){
            return getCurrentValue();
        }
        nNet.neuronEvalDepth += 1;
        if (nNet.neuronEvalDepth > 45) {
            throw new ChaosNetException("Max Eval Depth Hit: " + this.nNet.entity.getCCNamespace() + "   " + this.id);
        }
        float totalScore = 0;
        for(NeuronDep neuronDep :dependencies){
            if(neuronDep.depNeuron == null){
                String orgNamespace = nNet.entity.getCCNamespace();
                throw new ChaosNetException("Missing `neuronDep.depNeuron` : " + orgNamespace + " " + neuronDep.depNeuronId);
            }

            neuronDep.evaluate();
            totalScore += neuronDep.getCurrentValue();
        }
        newValue = activator.activateValue(totalScore);//sigmoid(totalScore);
        hasBeenEvaluated = true;
        nNet.neuronEvalDepth -= 1;
        setCurrentValue(newValue);
        return getCurrentValue();

    }
    public void populate(){
        for (NeuronDep neuronDep: dependencies) {
            neuronDep.populate(this.nNet);
        }
    }
  /*  public float sigmoid(float x){
        return (float) (1 / (1 + Math.exp(-x)));
    }*/

    /**
     * This does an aproximation between -1 and 1 of its what the value coming in may have been
     * @param x
     * @return
     */
    public float reverseSigmoid(float x){
        return ((x * 2) -1);
    }
    public void attachNNet(NeuralNet neuralNet) {
        this.nNet = neuralNet;
    }
    public String toString(){
        String response = toAbreviation();
        response += " ";

        response += getPrettyCurrValue();

        return response;
    }
    public String toAbreviation(){
       String name =  this.getClass().getSimpleName();

        String abreviation = "";
        int i = 0;
        try {
            for (i = 0; i < name.length() - 1; i++) {
                String letter = name.substring(i, i + 1);
                if (letter.equals(letter.toUpperCase())) {
                    abreviation += letter;
                }
            }
        }catch (Exception e){
            String message = e.getMessage();
            message += " " + i + " " + abreviation;
            throw new ChaosNetException(message);
        }
        return abreviation;

    }
    public String toLongString(){
        String response = this.getClass().getSimpleName().replace("Input","") + " " + this.activator.getClass().getSimpleName() + " ";
        response += getPrettyCurrValue();
        return response;
    }
    public float getPrettyCurrValue(){
        float prettyCurrValue = (Math.round(this._currentValue * 100f) / 100f);
        return prettyCurrValue;
    }

    public boolean getHasBeenEvaluated() {
        return hasBeenEvaluated;
    }


}
