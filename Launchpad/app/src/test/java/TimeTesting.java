import org.junit.Test;

import java.time.LocalDateTime;

public class TimeTesting {
    @Test
    public void testTime () {
        double dif = 5;
        LocalDateTime start = LocalDateTime.parse("2017-02-03T12:30:59");
        System.out.println(start);
        LocalDateTime end = LocalDateTime.parse("2017-02-03T12:31:05");
        System.out.println(end);
        System.out.println(start.compareTo(end.minusSeconds((long) dif)));
    }
}
