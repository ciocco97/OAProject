package Instance;

import java.util.ArrayList;

public class Instance {
    private ArrayList<InstRequest> requests = new ArrayList();
    private ArrayList<InstActivity> activities = new ArrayList();
    private int num_activities;
    private int num_families;
    private int num_timeslots;
    private int num_days;
    private int num_proxyRequests;
    private int num_categories;

    public Instance(int num_families, int num_activities, int num_timeslots, int num_days, int num_categories, int num_proxyRequests) {
        this.num_activities = num_activities;
        this.num_families = num_families;
        this.num_timeslots = num_timeslots;
        this.num_days = num_days;
        this.num_proxyRequests = num_proxyRequests;
        this.num_categories = num_categories;
    }

    public void addRequest(int ID, int unit, int activity, int day, int timeslot, int gain, float penalty_A, float penalty_D, float penalty_T, int proxy) {
        requests.add(new InstRequest(ID, unit, activity, day, timeslot, gain, penalty_A, penalty_D, penalty_T, proxy));
    }

    public void addActivity(int ID, int category, int capacity) {
        activities.add(new InstActivity(ID, category, capacity));
    }

    public int getNum_activities() {
        return num_activities;
    }

    public void setNum_activities(int num_activities) {
        this.num_activities = num_activities;
    }

    public int getNum_families() {
        return num_families;
    }

    public void setNum_families(int num_families) {
        this.num_families = num_families;
    }

    public int getNum_timeslots() {
        return num_timeslots;
    }

    public void setNum_timeslots(int num_timeslots) {
        this.num_timeslots = num_timeslots;
    }

    public int getNum_days() {
        return num_days;
    }

    public void setNum_days(int num_days) {
        this.num_days = num_days;
    }

    public int getNum_proxyRequests() {
        return num_proxyRequests;
    }

    public void setNum_proxyRequests(int num_proxyRequests) {
        this.num_proxyRequests = num_proxyRequests;
    }

    public int getNum_categories() {
        return num_categories;
    }

    public void setNum_categories(int num_categories) {
        this.num_categories = num_categories;
    }

    public int getNum_requests() {
        return this.requests.size();
    }

    /**
     * @param activityIndex the index of the activity
     * @return return the category's code of the activity corrisponding to the index
     */
    public int getCategoryByActivity(int activityIndex) {
        return this.activities.get(activityIndex).getCategory();
    }

    public int getTimeByRequest(int index) {
        return this.requests.get(index).getTimeslot();
    }

    public int getDayByRequest(int index) {
        return this.requests.get(index).getDay();
    }

    public int getGainByRequest(int index) {
        return this.requests.get(index).getGain();
    }

    public int getActivityByRequest(int index) {
        return this.requests.get(index).getActivity();
    }

    public float getPenaltyAByRequest(int index) {
        return requests.get(index).getPenalty_A();
    }

    public float getPenaltyTByRequest(int index) {
        return requests.get(index).getPenalty_T();
    }

    public float getPenaltyDByRequest(int index) {
        return requests.get(index).getPenalty_D();
    }

    public int getProxyByRequest(int i) {
        return requests.get(i).getProxy();
    }

}
