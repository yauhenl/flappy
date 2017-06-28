package com.yauhenl.flappy.neat;

/**
 * Super TODO: Srediti ceo kod da bude lepo napisano i zakomentarisano :D
 * Neke ideje povadjene odavdije:
 * https://github.com/vivin/DigitRecognizingNeuralNetwork/blob/master/src/main/java/net/vivin/neural/NeuralNetwork.java
 * <p>
 * iNodes: oni na ciji output moze da se nadoveze
 * outpuees: oni na ciji ulaz moze da se nadoveze
 * ioNodes: oni koji mogu i jedno i drugo :D
 **/


import com.yauhenl.flappy.game.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Network {
    private Random randomGen = new Random();
    private ArrayList<Neuron> sensors = new ArrayList<>();
    private ArrayList<Neuron> iNodes = new ArrayList<>();
    private ArrayList<Neuron> oNodes = new ArrayList<>();
    private ArrayList<Neuron> ioNodes = new ArrayList<>();

    private Neuron actuator = new Neuron();

    public Network() {
        oNodes.add(actuator);
    }

    public double propagate()
    {
        Resources.visitedNeurons.clear();
        return actuator.activate();
    }

    @SuppressWarnings("Duplicates")
    public Network copy() {

        /* Needed for breadth first graph traversal */
        LinkedList<Neuron> queue = new LinkedList<>();
        HashMap<Neuron, Neuron> map = new HashMap<>();

        /* Cloned network */
        Network copyNetwork = new Network();

        /* We will start traversal from the actuator */
        Neuron originalActuator = this.actuator;

        Neuron copyActuator = new Neuron();
        copyNetwork.setActuator(copyActuator);

        /* Add the first in line for processing, the original actuator */
        queue.add(originalActuator);
        map.put(originalActuator, copyActuator);


        ArrayList<Neuron> copyioNodes = new ArrayList<>();
        ArrayList<Neuron> copyiNodes = new ArrayList<>();
        ArrayList<Neuron> copyoNodes = new ArrayList<>();
        ArrayList<Neuron> copySensors = new ArrayList<>();

        for( Neuron s : sensors ) {
            Neuron temp = new Neuron();
            map.put(s, temp);
            queue.add(s);
            copySensors.add(temp);
            copyiNodes.add(temp);
        }

        while (!queue.isEmpty()) {
            /* Node we are currently processing */
            Neuron originalNode = queue.pop();

            ArrayList<Synapse> inputSynapses = originalNode.getInputs();
            ArrayList<Synapse> outputSynapses = originalNode.getOutputs();

            /* Clone of the original node that is being processed */
            Neuron copyNode = map.get(originalNode);

            for (Synapse inputSynapse : inputSynapses) {
                /* Get the real input node */
                Neuron inputOriginal = inputSynapse.getSource();

                /* If the copy of the inputOriginal doesn't exist, create it. Otherwise just connect */
                if (!map.containsKey(inputOriginal)) {
                    Neuron inputCopy = new Neuron();
                    copyioNodes.add(inputCopy);
                    Synapse synapseCopy = new Synapse(inputCopy, copyNode, inputSynapse.getWeight());
                    inputCopy.addOutput(synapseCopy);
                    copyNode.addInput(synapseCopy);
                    map.put(inputOriginal, inputCopy);
                    queue.add(inputOriginal);
                } else {
                    /* Get the copy of the real node since it exists */
                    Neuron inputCopy = map.get(inputOriginal);

                    boolean alreadyExists = false;
                    for(Synapse n : inputCopy.getOutputs()) {
                        if( n.getSource().equals(inputCopy) && n.getDestination().equals(copyNode)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if(!alreadyExists) {
                         /* Create input synapse between cloned nodes */
                        Synapse copySynapse = new Synapse(inputCopy, copyNode, inputSynapse.getWeight());
                        copyNode.addInput(copySynapse);
                        inputCopy.addOutput(copySynapse);
                    }
                }
            }
            for (Synapse outputSynapse : outputSynapses) {
                /* Get the real output node */
                Neuron outputOriginal = outputSynapse.getDestination();

                /* If the copy of the outputOriginal doesn't exist, create it. Otherwise just connect */
                if (!map.containsKey(outputOriginal)) {
                    Neuron outputCopy = new Neuron();
                    copyioNodes.add(outputCopy);
                    Synapse syn = new Synapse(copyNode, outputCopy, outputSynapse.getWeight());
                    copyNode.addOutput(syn);
                    outputCopy.addInput(syn);
                    map.put(outputOriginal, outputCopy);
                    queue.add(outputOriginal);
                } else {
                    /* Get the copy of the real node since it exists */
                    Neuron outputCopy = map.get(outputOriginal);

                    boolean alreadyExists = false;
                    for(Synapse n : outputCopy.getInputs()) {
                        if(n.getSource().equals(copyNode) && n.getDestination().equals(outputCopy)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if(!alreadyExists) {
                        /* Create output synapse between cloned nodes */
                        Synapse copySynapse = new Synapse(copyNode, outputCopy, outputSynapse.getWeight());
                        copyNode.addOutput(copySynapse);
                        outputCopy.addInput(copySynapse);
                    }
                }

            }
        }

        /* Set newly created sensors */
        copyNetwork.setSensors(copySensors);

        /* Within cloned network, remove sensors from ioNodes, add to iNodes*/
        //for (Neuron copySensor : copySensors) {
         //   copyioNodes.remove(copySensor);
         //   copyiNodes.add(copySensor);
       // }

        /* Add all nodes (except sensors, and actuator) to iNodes and oNodes */
        for (Neuron ioNode : copyioNodes) {
            copyiNodes.add(ioNode);
            copyoNodes.add(ioNode);
        }

        /* Add actuator to oNodes */
        copyoNodes.add(copyActuator);

        copyNetwork.setInputNodes(copyiNodes);
        copyNetwork.setOutputNodes(copyoNodes);
        copyNetwork.setInputOutputNodes(copyioNodes);

        // Crap code ahead ================================ WOO HOO WATCH ME ==========================================
        // TODO: Erase this souts once we create a few working networks
//        System.out.println("Length of ioNodes " + copyioNodes.size());
//        System.out.println("Length of iNodes  " + copyiNodes.size() + " it should be ioNodes + 3");
//        System.out.println("Length of oNodes  " + copyoNodes.size() + " it should be ioNodes + 1");
//        System.out.println(copyNetwork.actuator);
//        System.out.println(copyActuator);
//        System.out.println(copyActuator.weightedSum);
//        for (Neuron n : copyNetwork.iNodes)
//        {
//            System.out.println(n.weightedSum);
//            System.out.println(n.getOutputs());
//            System.out.println(n.getInputs());
//        }
        // Crap code finished ============================= WOO HOO WATCH ME ==========================================

        return copyNetwork;
    }

    public void setInputValues(double[] input) {
        if (input.length != sensors.size()) {
            throw new IllegalArgumentException("Array size of neural network input values must be the same number of input sensors!");
        }

        int i = 0;
        for (Neuron sensor : sensors) {
            sensor.setOutput(input[i]);
            i++;
        }
    }

    public void mutateAddNeuron() {
        Neuron temp = new Neuron();

        Neuron source = getRandomNeuron(iNodes);
        Synapse synIn = new Synapse(source, temp, randomGen.nextDouble());
        source.addOutput(synIn);
        temp.addInput(synIn);

        Neuron destination = getRandomNeuron(oNodes);
        Synapse synOut = new Synapse(temp, destination, randomGen.nextDouble());
        destination.addInput(synOut);
        temp.addOutput(synOut);

        iNodes.add(temp);
        oNodes.add(temp);
        ioNodes.add(temp);
    }

    public void mutateSplice() {
        Neuron source = getRandomNeuron(iNodes);
        Synapse sourceSynapse = getRandomSynapse(source.getOutputs());
        Neuron destination = sourceSynapse.getDestination();

        Neuron temp = new Neuron();

        sourceSynapse.setDestinationNeuron(temp);

        destination.getInputs().remove(sourceSynapse);

        Synapse tempSynapse = new Synapse(temp, destination, randomGen.nextDouble());

        temp.getOutputs().add(tempSynapse);
        destination.getInputs().add(tempSynapse);

        iNodes.add(temp);
        oNodes.add(temp);
        ioNodes.add(temp);
    }

    public void mutateAddSynapse() {

        Neuron source = getRandomNeuron(iNodes);
        Neuron dest = source;
        while (dest.equals(source)) {
            dest = getRandomNeuron(oNodes);
        }

        Synapse syn = new Synapse(source, dest, Math.random());
        source.addOutput(syn);
        dest.addInput(syn);
    }

    public void mutateRemoveNeuron() {
        if (ioNodes.size() != 0) {
            Neuron temp = getRandomNeuron(ioNodes);
            for (Synapse syn : temp.getInputs()) {
                syn.getSource().getOutputs().remove(syn);
            }
            for (Synapse syn : temp.getOutputs()) {
                syn.getDestination().getInputs().remove(syn);
            }

            temp.getInputs().clear();
            temp.getOutputs().clear();

            oNodes.remove(temp);
            iNodes.remove(temp);
            ioNodes.remove(temp);
        }
    }

    public void mutateChangeWeights() {
        if (ioNodes.size() != 0) {
            Neuron temp = getRandomNeuron(ioNodes);
            for (Synapse s : temp.getInputs()) {
                s.setWeight(randomGen.nextDouble());
            }
        }
    }

    public void mutate() {
        if (randomGen.nextDouble() > 0.4) {
            int mutationType = randomGen.nextInt(4);
            if (mutationType == 0)
                mutateAddNeuron();
            else if (mutationType == 1)
                mutateRemoveNeuron();
            else if (mutationType == 2)
                mutateChangeWeights();
            else if (mutationType == 3)
                mutateAddSynapse();
        }
        mutateChangeWeights();
    }

    public Neuron getRandomNeuron(ArrayList<Neuron> neuronList) {
        int index = randomGen.nextInt(neuronList.size());
        return neuronList.get(index);
    }

    public Synapse getRandomSynapse(ArrayList<Synapse> synapseList) {
        int index = randomGen.nextInt(synapseList.size());
        return synapseList.get(index);
    }

    public void setInputNodes(ArrayList<Neuron> iNodes) {
        this.iNodes = iNodes;
    }

    public void setOutputNodes(ArrayList<Neuron> oNodes) {
        this.oNodes = oNodes;
    }

    public void setInputOutputNodes(ArrayList<Neuron> ioNodes) {
        this.ioNodes = ioNodes;
    }

    public void setSensors(ArrayList<Neuron> sensors) {
        for (Neuron sensor : sensors) {
            this.iNodes.add(sensor);
            this.sensors.add(sensor);
        }
    }

    public void setActuator(Neuron actuator) {
        this.actuator = actuator;
        this.oNodes.add(actuator);
    }

}
