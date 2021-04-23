package Instance;

public class InstActivity {
    int ID;
    int category;
    int capacity;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public InstActivity(int ID, int category, int capacity) {
        this.ID = ID;
        this.category = category;
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "InstActivity{" + "ID=" + ID + ", category=" + category + ", capacity=" + capacity + '}';
    }


}
