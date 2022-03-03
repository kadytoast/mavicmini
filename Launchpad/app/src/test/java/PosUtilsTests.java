import static com.dji.launchpad.Utils.Calc.calcHeadingDifference;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PosUtilsTests {
    @Test
    public void calcHeadingDifferenceCorrect() {

        // testvals, val a, val b, answer
        int[][] testvals = {
                {-30, 60, 90},
                {150, 30, -120},
                {-180, 180, 0},
                {0, 0, 0},
                {-90, 90, -180},
                {0, 0, 0},
                {116, 32, -84},
                {130, -13, -143},
                {-83, -21, 62},
                {49, -98, -147}
        };

        for (int[] vals : testvals) {
            System.out.println(vals[0] + ", " + vals[1] + ", " + calcHeadingDifference(vals[0], vals[1]));
            assertEquals(vals[2], calcHeadingDifference(vals[0], vals[1]), 0);
        }
    }
}
