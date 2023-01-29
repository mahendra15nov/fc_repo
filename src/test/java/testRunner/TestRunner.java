package testRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features/DemoGet.feature",
        glue = {"stepdefinitions"},
        plugin = {"pretty", "html:target/cucumber-reports","json:target/cucumber.json"}
)
public class TestRunner  extends AbstractTestNGCucumberTests {
}

