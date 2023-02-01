package stepdefinitions;

import com.google.gson.Gson;
import com.sun.source.tree.AssertTree;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.PostPojo;

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
    @Given("^POST the entries from public API$")
    public void postPublicEntriesData() {
        PostPojo obj = new PostPojo();
        obj.setBody("This is a new blog post placeholder test.");
        obj.setTitle("New Blog Post");
        obj.setId(10);
        obj.setUserId(1000);
        Gson g = new Gson();
       String body =  g.toJson(obj);
        response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(body)
                .post("https://jsonplaceholder.typicode.com/posts");
    }

    @When("Validate the response code as {string}")
    public void validateResponseCode(String statusCode) {
        System.out.println(response.getStatusCode());
        // Validate status code
        int statusCode1 = response.getStatusCode();
        Assert.assertEquals(statusCode1+"", statusCode);
    }

    @Then("^Validate the results$")
    public void validateResults() {
        System.out.println(response.getBody().asString());
        System.out.println(response.asString());
        System.out.println(response.getHeader("content-type"));
        //JSONObject myObject = new JSONObject(response.getBody().asPrettyString());
        //Assert.assertEquals(myObject.get("title").toString(),"New Blog Post");
    }

}
