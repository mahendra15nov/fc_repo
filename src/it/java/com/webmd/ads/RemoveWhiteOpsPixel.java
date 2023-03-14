package com.webmd.ads;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;

//PPE-176731 Remove White Ops pixel from Medscape

public class RemoveWhiteOpsPixel extends AdsCommon {

	public static boolean omnitureFlag = false, pageSourceFlag = false;
	public String[][] input;

	// Getting all the URLs available to test, in before method. In one test
	// case, open all the urls and verify.

	@BeforeMethod()
	public void beforeMethod() {
		getDriver();
		// login();
		input = XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "pageFair");
	}

	@AfterClass()
	public void quitBrowser() {
		getDriver().quit();
		getServer().stop();
	}
	// This method will verify whether particular call tracked under network
	// traffic or not

	private void verifyOmniture() {
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("s.tagsrvcs.com/2/587654/analytics.js")) {
				generateFailReport("tagsrvcs found in Network calls");
				omnitureFlag = true;
				break;
			}
		}
	}

	// This method will verify whether particular values tracked under page
	// source or not
	private void verifyPageSource() {

		if (getDriver().getPageSource().toString().contains("//s.tagsrvcs.com/2/587654/analytics.js")) {
			generateFailReport("tagsrvcs found in pagesource");
			pageSourceFlag = true;
		}
	}



	@Test(dataProvider = "dataProvider")
	public void test(String user) {
		// adsLogin(user);

		for (int i = 0; i < input.length; i++) {
			getServer().newHar();
			getDriver().get(input[i][1]);
			generateInfoReport("Verifying for " + input[i][1]);
			verifyOmniture();
			verifyPageSource();
		}
		try {
			Assert.assertFalse(pageSourceFlag);
			generatePassReport("No traces observed with s.tagsrvcs.com in page source");
		} catch (AssertionError e) {
			generateFailReport("Traces observed with s.tagsrvcs.com in page source");
		}

		try {
			Assert.assertFalse(omnitureFlag);
			generatePassReport("No traces observed with s.tagsrvcs.com in Network traffic");
		} catch (AssertionError e) {
			generateFailReport("Traces observed with s.tagsrvcs.com in Network traffic");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "kasupada,medscape" } };
	}
}
