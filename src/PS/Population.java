package PS;

import Gurobi.GRBRequest;

import java.util.ArrayList;

public class Population {
    private ArrayList<GRBRequest[]> population;
    private GenerationStrategy god;

    public Population(GRBRequest[] startingPoint, int size) {
        this.population = god.generatePopulation(startingPoint, size);
    }
}
