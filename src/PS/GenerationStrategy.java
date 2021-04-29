package PS;

import Gurobi.GRBRequest;

import java.util.ArrayList;

public interface GenerationStrategy {

    public  ArrayList<GRBRequest[]> generatePopulation(GRBRequest[] requests, int size);
}
