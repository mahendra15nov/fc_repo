package com.webmd.ads;

import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;
import com.webmd.general.common.XlRead;

/**
 * 
 * @author tnamburi PPE-207512: Enable ads on all MDP pages (FE)
 */

public class AdsOnODPPages extends AdsCommon {

	@Test(dataProvider = "dataProvider")
	public void testODPpages(String URL) {//
		// String URL = "https://www.medscape.org/viewarticle/915898";
		getDriver();
		setDescription("Validating the page: " + URL);
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(URL);

		try {
			Assert.assertTrue(verifySpecificCallPresence(AdsConstantns.AD_CALL));
			generatePassReportWithNoScreenShot("Ad call being made");
		} catch (AssertionError e) {
			generateFailReport("Ad call not made");
		}
		try {
			String ads2Ignore = getConsoleValue("ads2_ignore");
			generateInfoReport("Ads2Ignore : " + ads2Ignore);
			Assert.assertFalse(ads2Ignore.contains("all=true"));
			generatePassReportWithNoScreenShot("Console value has no all");
		} catch (AssertionError e) {
			generateFailReport("Console has all ads ignored");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/odpPages.xls", "Sheet1");
	}

}
