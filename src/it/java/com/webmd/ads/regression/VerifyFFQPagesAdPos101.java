package com.webmd.ads.regression;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

/**
 * @author sandeep.gowada
 * 
 *         PPE-211937: HOTFIX: Unhide pos 101 on all FFQ pages
 *
 */
public class VerifyFFQPagesAdPos101 extends AdsCommon {

	private static final String EXPECTED_101 = "728x90|970x250|970x90";
	private static final String TESTDATA = "FFQPages.xls";

	@Test(dataProvider = "FFQdataProvider", groups = { "Desktop" })
	public void testAdpos101Unhidden(String testURL) {

		generateInfoReport(
				"Validating Fast Five Quiz Pages on medscape for Ad pos 101 to be visible physically while it is avaialable in the ad call");
		testURL = testURL.replace("env.", env);
		getDriver();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD") && !env.equalsIgnoreCase("STAGING"))
			testURL = testURL.replace("medscape", env + "medscape").replace("staging.", "");
		if (testURL.contains("registration_ab.do") || testURL.contains("login")) {
			logout(getProperty("username"));
		}
		login("infosession33", "medscape");
		getServer().newHar();
		generateInfoReport("Launching page: " + testURL);
		getDriver().get(testURL);
		if (!is404(getDriver().getTitle())) {
			waitForAdCallFound();
			if (verifySpecificCallPresence(AdsConstantns.AD_CALL)) {
				assertAdCall();
				assertAdSize();
				assertAdPhysicallyLoaded("101");

			}
		}

	}

	private void assertAdCall() {

		generateInfoReport("Validating the ad call for pos 101");
		try {
			Assert.assertTrue(verifySpecificAdPresenceInSecurePubadCall("101"));
			generatePassReport("Ad pos 101 is available in the ad call");
		} catch (AssertionError e) {
			generateFailReport("Ad pos 101 is expected in the ad call for FFQ Pages. Fail url: "
					+ getDriver().getCurrentUrl() + " Exception message: " + e.toString());
		}
	}

	private void assertAdSize() {

		generateInfoReport("Validating the ad size for pos 101");
		try {
			String pos101Size = getSizesForSpecificPositionFromAdCall("101");
			generateInfoReport("Ad size for pos 101 found: " + pos101Size);
			Assert.assertEquals(pos101Size, EXPECTED_101);
			generatePassReport("Ad pos 101 loaded with correct size: " + EXPECTED_101);
		} catch (AssertionError e) {
			generateFailReport("Ad size incorrect. Expected: " + EXPECTED_101 + " Exception message: " + e.toString());
		}
	}

	private void assertAdPhysicallyLoaded(String position) {

		generateInfoReport("Validating the physical placement of Ad pos 101");
		try {
			boolean isLoaded = verifySpecificPositionLoadedOnPage(position);
			Assert.assertTrue(isLoaded);
			generatePassReport("Ad pos 101 physically loaded on page: " + getDriver().getCurrentUrl());
		} catch (Exception e) {
			generateFailReport("Exception while gettting the physical postion of ad");
		} catch (AssertionError e) {
			generateFailReport("Ad not loaded physically on page: " + getDriver().getCurrentUrl());
		}
	}

	@DataProvider
	public String[] FFQdataProvider() {

		return getURLs(TESTDATA, "url");

	}
}
