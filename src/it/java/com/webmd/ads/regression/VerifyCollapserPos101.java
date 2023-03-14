package com.webmd.ads.regression;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * PPE-213378 Modify sticky pos 101 logic to handle collapsers from the ad
 * server
 */
public class VerifyCollapserPos101 extends AdsCommon {

	private static final String TESTDATA = "CollapserTestData101.xls";

	@Test(dataProvider = "dataProvider", groups = { "Desktop", "CollapserTest" })
	public void verifypos101Collapse(String url) {

		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD") && !env.equalsIgnoreCase("STAGING"))
			url = url.replace("medscape", env + "medscape").replace("staging.", "");
		if (url.contains("registration_ab.do") || url.contains("login")) {
			logout(getProperty("username"));
		}

		setDescription(
				"Verify that when the collapser is served from the ad server, the ad pos 101 should not be visible on the page and there shouldnt be any blank space on the page");
		getDriver();
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		generateInfoReport("Loaded: " + url);

		try {
			WebElement adpos101 = getDriver().findElement(By.xpath("//div[@id='ads-pos-101']"));
			generateInfoReport("Ad pos 101 is available in the DOM");
			WebElement adTagHeader = getDriver().findElement(By.xpath("//div[@id='adtagheader']"));
			String style = adTagHeader.getAttribute("style");
			String style101 = adpos101.getAttribute("style");
			if (isCollapserServed("101")) {
				Assert.assertTrue(
						style.equalsIgnoreCase("display: none;") || style101.equalsIgnoreCase("display: none;"));
				generatePassReport(
						"Ad pos 101 is collapsed and does not appear on the page as expected when collapser script is served from Ad server");
			}

		} catch (NoSuchElementException e) {
			generateInfoReport("Ad pos 101 not found. " + e.getMessage());
		} catch (AssertionError e) {
			generateFailReport("Pos 101 is loaded on the page even when collapser script is served from Ad server");
		}

	}

	@DataProvider
	public String[] dataProvider() {

		return getURLs(TESTDATA, "URL");
	}
}
