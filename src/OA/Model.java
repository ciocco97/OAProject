package OA;

import Instance.CategoriesArrayBuilder;
import Instance.Instance;

import java.util.LinkedList;

public class Model {
    private int numOfRequests;
    private int numOfActivities;
    private int numOfCategories;
    private Instance instance;
    private LinkedList<Request> K = new LinkedList();

    public Model(String filename) throws Exception {
        FileParser parser = new FileParser(filename);
        instance = parser.getIstance();
        this.numOfRequests = instance.getNum_requests();
        this.numOfActivities = instance.getNum_activities();
        this.numOfCategories = instance.getNum_categories();
        CategoriesArrayBuilder arBuilder = new CategoriesArrayBuilder(instance);

        int days = instance.getNum_days();
        int times = instance.getNum_timeslots();

        int t, d, g, a, proxy;
        float phiA, phiD, phiT;
        boolean[] ts, ds, mapA, as;
        Request req;

        for (int i = 0; i < instance.getNum_requests(); i++) {
            t = instance.getTimeByRequest(i);
            ts = generateBoolArray(t, times);
            d = instance.getDayByRequest(i);
            ds = generateBoolArray(d, days);
            a = instance.getActivityByRequest(i);
            as = generateBoolArray(a, instance.getNum_activities());
            mapA = arBuilder.getArrayByCategory(instance.getCategoryByActivity(a));
            g = instance.getGainByRequest(i);
            phiA = instance.getPenaltyAByRequest(i);
            phiD = instance.getPenaltyDByRequest(i);
            phiT = instance.getPenaltyTByRequest(i);
            proxy = instance.getProxyByRequest(i);

            req = new Request(ts, t, ds, d, as, mapA, a, g, phiA, phiD, phiT, proxy);
            K.add(req);

        }
    }

    public Request getRequest(int index) {
        return this.K.get(index);
    }

    public int getNumOfRequests() {
        return numOfRequests;
    }

    public void setNumOfRequests(int numOfRequests) {
        this.numOfRequests = numOfRequests;
    }

    private boolean[] generateBoolArray(int index, int size) {
        {
            boolean[] array = new boolean[size];
            for (int i = 0; i < size; i++) {
                array[i] = false;
            }
            array[index] = true;
            return array;
        }
    }

    public int getNumOfTimeSlots() {
        return instance.getNum_timeslots();
    }

    public int getNumOfDays() {
        return instance.getNum_days();
    }

    public int getNumOfActivities() {
        return this.numOfActivities;
    }

    public double[] getGains() {
        double gains[] = new double[numOfRequests];
        for (int i = 0; i < numOfRequests; i++) {
            gains[i] = instance.getGainByRequest(i);
        }
        return gains;
    }

    public int getMAXNumProxyRequests() { return instance.getNum_proxyRequests(); }

}
