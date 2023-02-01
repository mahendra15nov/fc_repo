package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeSuite;

@CucumberOptions(
        features = "src/test/resources/features/DemoGet.feature",
        glue = {"stepdefinitions"},
        plugin = {"pretty", "html:target/cucumber-reports.html","json:target/cucumber.json"},
        tags = "@SmokeTest"
)
public class TestRunner  extends AbstractTestNGCucumberTests {
}

