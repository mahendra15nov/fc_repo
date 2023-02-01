Feature: Verify that get method successfully fetch the data from end point

  Scenario: Verification of GET - First
    Given Get the entries from public API
    When Validate the response code as "200"
    Then Validate the results

  Scenario: Verification of GET - First
    Given Get the entries from public API
    When Validate the response code as "200"
    Then Validate the results

  @SmokeTest
  Scenario: Verification of POST - First
    Given POST the entries from public API
    When Validate the response code as "201"
    Then Validate the results

