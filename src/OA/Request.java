package OA;

public class Request {
    private boolean[] timeslots;
    private int time;
    private boolean[] days;
    private int day;
    private boolean[] activities;
    private boolean[] activities_of_category;
    private int activity;
    private float gain;
    private float penalty_A;
    private float penalty_D;
    private float penalty_T;
    private int PROXY;

    public Request(boolean[] timeslots, int time, boolean[] days, int day, boolean[] activities, boolean[] activities_of_category, int activity, float gain, float penalty_A, float penalty_D, float penalty_T, int proxy) {
        this.timeslots = timeslots;
        this.time = time;
        this.days = days;
        this.day = day;
        this.activities = activities;
        this.activities_of_category = activities_of_category;
        this.activity = activity;
        this.gain = gain;
        this.penalty_A = penalty_A;
        this.penalty_D = penalty_D;
        this.penalty_T = penalty_T;
        this.PROXY = proxy;
    }

    @Override
    public String toString() {
        String output = new String();
        output += "Timeslot: ";
        for (int i = 0; i < timeslots.length; i++) {
            output += timeslots[i] == false ? "0" : "1";
        }
        output += "\nDays: ";
        for (int i = 0; i < days.length; i++) {
            output += days[i] == false ? "0" : "1";
        }
        output += "\nInstActivity: ";
        for (int i = 0; i < activities.length; i++) {
            output += activities[i] == false ? "0" : "1";
        }
        output += "\nmapA: ";
        for (int i = 0; i < activities_of_category.length; i++) {
            output += activities_of_category[i] == false ? "0" : "1";
        }

        return output;
    }

    public boolean[] getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(boolean[] timeslots) {
        this.timeslots = timeslots;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean[] getDays() {
        return days;
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean[] getActivities() {
        return activities;
    }

    public void setActivities(boolean[] activities) {
        this.activities = activities;
    }

    /**
     * Ritorna un array di lunghezza instance.getNum_activities() di boolean
     * arBuilder.getArrayByCategory(instance.getCategoryByActivity(this.activity))
     * @return activities_of_category
     */
    public boolean[] getActivities_of_category() {
        return activities_of_category;
    }

    public void setActivities_of_category(boolean[] activities_of_category) {
        this.activities_of_category = activities_of_category;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getPenalty_A() {
        return penalty_A;
    }

    public void setPenalty_A(float penalty_A) {
        this.penalty_A = penalty_A;
    }

    public float getPenalty_D() {
        return penalty_D;
    }

    public void setPenalty_D(float penalty_D) {
        this.penalty_D = penalty_D;
    }

    public float getPenalty_T() {
        return penalty_T;
    }

    public void setPenalty_T(float penalty_T) {
        this.penalty_T = penalty_T;
    }

    public int getPROXY() {
        return PROXY;
    }

    public void setPROXY(int PROXY) {
        this.PROXY = PROXY;
    }


}
