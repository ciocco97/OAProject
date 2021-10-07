package OA;

import Instance.Instance;
import Log.MyLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileParser {
    private File file;

    public FileParser(String path) {
        file = new File(path);
    }

    public Instance getIstance() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine(); // the first line is a set of numbers
        String[] nums = line.split(" "); // the numbers are splitted by the space character
        int num_families = Integer.parseInt(nums[0]);
        int num_activities = Integer.parseInt(nums[1]);
        int num_timeslots = Integer.parseInt(nums[2]);
        int num_days = Integer.parseInt(nums[3]);
        int num_categories = Integer.parseInt(nums[4]);
        int num_proxiesRequests = Integer.parseInt(nums[5]);

        Instance istance = new Instance(num_families, num_activities, num_timeslots, num_days, num_categories, num_proxiesRequests);
        br.readLine(); // skip a line that contains "ACTIVITIES"

        int a, c, L;
        for (int i = 0; i < num_activities; i++) {
            line = br.readLine();
            nums = line.split(" ");
            a = Integer.parseInt(nums[0]); // id of the activity
            c = Integer.parseInt(nums[1]); // id of the categiry
            L = Integer.parseInt(nums[2]); // capacity of the activity
            istance.addActivity(a, c, L);
        }

        br.readLine(); //skip a line that contains "REQUESTS"

        int r, u, d, t, g, P;
        float p1, p2, p3;
        while ((line = br.readLine()) != null) {
            nums = line.split(" ");
            r = Integer.parseInt(nums[0]);
            u = Integer.parseInt(nums[1]);
            a = Integer.parseInt(nums[2]);
            d = Integer.parseInt(nums[3]);
            t = Integer.parseInt(nums[4]);
            g = Integer.parseInt(nums[5]);
            p1 = Float.parseFloat(nums[6]);
            p2 = Float.parseFloat(nums[7]);
            p3 = Float.parseFloat(nums[8]);
            P = Integer.parseInt(nums[9]);
            istance.addRequest(r, u, a, d, t, g, p1, p2, p3, P);
        }


        return istance;
    }
}
