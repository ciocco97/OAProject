package PS;

import Gurobi.GRBRequest;

import java.util.ArrayList;

public class RandomStrategy implements GenerationStrategy {

    @Override
    /**
     * in this method we are generating the population totally randomly, with a constant probability which is equal to
     * 1/size, where size is the components of the population.
     */
    public ArrayList<GRBRequest[]> generatePopulation(GRBRequest[] requests, int size) {
        ArrayList<GRBRequest[]> pop = new ArrayList();
        for (GRBRequest request : requests) {
            for (int i = 0; i < size; i++) {

            }


        }
        return null;
    }
}
