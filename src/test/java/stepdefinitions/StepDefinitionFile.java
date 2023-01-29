package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;

public class StepDefinitionFile extends Base {
    Response response;

    @Given("^Get the entries from public API$")
    public void getPublicEntriesData() {
        response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .param("lon","113.17")
                .param("lat","23.09")
                .param("product","astro")
                .param("output","json")
                .get("http://www.7timer.info/bin/api.pl");
    }

    @When("^Validate the response code as 200$")
    public void validateResponseCode() {
        System.out.println(response.getStatusCode());
        // Validate status code
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200);
    }

    @Then("^Validate the results$")
    public void validateResults() {
        System.out.println(response.getBody().asString());
        System.out.println(response.asString());
        System.out.println(response.getHeader("content-type"));

    }

}
