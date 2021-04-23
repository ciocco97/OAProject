package Instance;

public class CategoriesArrayBuilder {
    private boolean[][] arrays;

    public CategoriesArrayBuilder(Instance instance) {
        arrays = new boolean[instance.getNum_categories()][];
        for (int i = 0; i < instance.getNum_categories(); i++) {
            arrays[i] = new boolean[instance.getNum_activities()];
            for (int j = 0; j < instance.getNum_activities(); j++)
                if (instance.getCategoryByActivity(j) == i) //if the category of the current activity is equal to the category we are looking for, we set the value in the array = 1
                    arrays[i][j] = true;
                else
                    arrays[i][j] = false;
        }
    }

    public boolean[] getArrayByCategory(int category) {
        return this.arrays[category];
    }

}
