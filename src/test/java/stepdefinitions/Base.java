package stepdefinitions;

import org.testng.annotations.*;

public class Base {

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        System.out.println("Before Suite");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        System.out.println("Before Class");
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        System.out.println("Before Method");
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        System.out.println("After Method");
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        System.out.println("After Class");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        System.out.println("After Suite");
    }
}
