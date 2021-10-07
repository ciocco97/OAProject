package Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyLog {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static void log(String message) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(formatter.format(now) + ":\t--- " + message + " ---");
    }

}
