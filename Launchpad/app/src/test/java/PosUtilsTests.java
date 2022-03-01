import static com.dji.launchpad.PosUtils.Calc.calcHeadingDifference;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PosUtilsTests {
    @Test
    public void calcHeadingDifferenceCorrect() {

        // testvals, val a, val b, answer
        int[][] testvals = {
                {-30, 60, 90},
                {150, 30, -120},
                {-180, 180, 0},
        };

        for (int[] vals : testvals) {
            assertEquals(vals[2], calcHeadingDifference(vals[0], vals[1]), 0);
        }
    }
}
