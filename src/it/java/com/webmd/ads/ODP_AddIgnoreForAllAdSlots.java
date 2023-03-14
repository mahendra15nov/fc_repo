package com.webmd.ads;

import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;
import com.webmd.general.common.XlRead;

/*
 * PPE-196957: ODP: Add ignore for all ad slots on ODP pages
 */

public class ODP_AddIgnoreForAllAdSlots extends AdsCommon {

	private String executeConsoleCommand() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		try {
			String ads2Ignore = jse.executeScript("return ads2_ignore").toString();
			return ads2Ignore;
		} catch (Exception e) {
			generateInfoReport("Exception while executing Console command");
		}
		return null;
	}

	@Test(dataProvider = "dataProvider", groups = { "testODP" })
	public void testAds2IgnoreInOdpPages(String URL) {
		login();
		URL = URL.replace("env.", env);
		getServer().newHar();
		getDriver().get(URL);
		generateInfoReport("Opened page: " + getDriver().getCurrentUrl());
		String ads2Ignore = executeConsoleCommand();
		try {
			Assert.assertTrue(ads2Ignore.contains("all=true"));
			generatePassReportWithNoScreenShot("All ads ignored on ODP pages");

		} catch (AssertionError e) {
			generateInfoReport("Not all ads Ignored, value from console" + ads2Ignore);
		} catch (NullPointerException e) {
			generateInfoReport("Ads2Ingore is null");
		}
		try {
			Assert.assertFalse(verifySpecificCallPresence(AdsConstantns.AD_CALL));
			generatePassReportWithNoScreenShot("No Ad call observed in ODP pages");
			scrollTillEnd();
			Assert.assertFalse(verifySpecificCallPresence("No Lazyload ad call observed on ODP page"));
		} catch (AssertionError e) {
			generateFailReport("ODP page has ad call");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/odpPages.xls", "Sheet1");
	}

}
