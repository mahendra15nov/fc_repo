import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTest {

    @Test()
    public void test() {
        Assert.assertEquals("First Line\nSecond Line", "Third Line\nFourth Line");
    }
}