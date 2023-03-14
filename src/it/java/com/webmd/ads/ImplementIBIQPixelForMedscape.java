package com.webmd.ads;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class ImplementIBIQPixelForMedscape extends AdsCommon {

	// @Test(dataProvider = "dataProvider")
	public void test(String[] input) {

		String URL = input[1];

		if (input[4].equalsIgnoreCase("pageload")) {
			getServer().newHar();
			getDriver().get(URL);
		} else if (input[4].equalsIgnoreCase("lazyload")) {
			getDriver().get(URL);
			getServer().newHar();
		}

		// code to get values
		String site = null;
		String vertical = null;

		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("??")) {
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().equalsIgnoreCase("site"))
						site = harNameValuePair.getValue();
					else if (harNameValuePair.getName().equalsIgnoreCase("vertical"))
						vertical = harNameValuePair.getValue();
				}
				break;
			}
		}

		try {
			if (URL.contains(".com"))
				Assert.assertEquals(site, "medscape.com");
			else if (URL.contains(".org"))
				Assert.assertEquals(site, "medscape.org");
			Assert.assertEquals(vertical, "health professional");
			generatePassReport("Vertical and Site values tracked properly");
		} catch (AssertionError e) {
			generateFailReport(
					"Vertical and Site values tracked as below" + "\nVertical = " + vertical + " and Site = " + site);
		}
	}

	@BeforeClass()
	public void login() {
		login(getProperty("username"), getProperty("password"));
	}

	@AfterClass()
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@Test(dataProvider = "dataProvider")
	public void verifyNetworkCall(String type, String URL) {

		getServer().newHar();
		getDriver().get(URL);
		generateInfoReport("Validating for " + URL);

		try {
			Assert.assertTrue(verifySpecificCallPresence("ibclick.stream/assets/js/track/dist/js/v1/tracker.min.js"));
			generatePassReport("Network call made to ibiq");
		} catch (AssertionError e) {
			generateFailReport("No call made to ibiq");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "Sheet1");
		/*
		 * return new String[][] {
		 * {"https://www.staging.medscape.com/cardiology"},
		 * {"https://www.staging.medscape.com/pediatrics"} };
		 */
	}

}
