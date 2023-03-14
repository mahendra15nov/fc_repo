package com.webmd.ads.regression;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

public class RegressionCasesWithOutLogin extends AdsCommon {

	/*
	 * Test www.medscape.com page as anon user
	 */

	@Test(groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb", "testAnon" })
	public void testHomePageAsAnon() {
		getDriver();
		getDriver().manage().deleteAllCookies();
		getServer().newHar();
		getDriver().get("http://www." + env + "medscape.com");
		try {
			Assert.assertTrue(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
			generatePassReportWithNoScreenShot("Ad call loaded on medscape.com page as an anon user");
		} catch (AssertionError e) {
			generateFailReport("Ad call not loaded on medscape.com page as an anon user");
		}
	}

	/*
	 * Test to validate login page ads
	 */
	@Test(groups = { "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb", "testAnon" })
	public void validateLoginPage() {
		String URL = "https://login." + env + "medscape.com/login/sso/getlogin?ac=401";
		getDriver().manage().deleteAllCookies();
		getServer().newHar();
		getDriver().get(URL.replace("staging.", ""));
		if (breakPoint.equals("1")) {
			generateInfoReport("Validating login page on Mobile break point");
			try {
				Assert.assertFalse(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
				generatePassReportWithNoScreenShot("No Ad call shown in login page for mobile breakpoint");
			} catch (AssertionError e) {
				generateFailReport("Ad call loaded on Login page for mobile break point");
			}
		} else {
			generateInfoReport("Validating login page on Desktop");
			String adType = null;
			try {
				Assert.assertTrue(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
				generatePassReportWithNoScreenShot("Ad call shown in login page for Desktop breakpoint");
				adType = validateAdPosOnPage(getDriver().findElement(By.xpath("//div[contains(@id,'101')]")));
				try {
					Assert.assertTrue(adType.equals("blankAd") | adType.equals("mediaAd"));
					generatePassReportWithNoScreenShot(adType + " is loaded on login page");
				} catch (AssertionError e) {
					generateFailReport("Problem in ad load on login page, Ad type loaded is " + adType);
				} catch (NullPointerException e) {
					generateFailReport("Ad type is Null on login page");
				}
			} catch (AssertionError e) {
				generateFailReport("No Ad call loaded on Login page for Desktop breakpoint");
			}
		}
	}

	/*
	 * Validate Login page for other language editions
	 */
	@Test(dataProvider = "dataProvider", groups = { "AdsRegression", "Desktop", "MobileWeb", "testAnon" })
	public void testLoginOfOtherLang(String URL) {
		getDriver();
		getDriver().manage().deleteAllCookies();
		getServer().newHar();
		getDriver().get(URL.replace("env.", env).replace("staging.", ""));
		try {
			scrollTillEnd();
			Thread.sleep(1000);
			Assert.assertFalse(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
			generatePassReportWithNoScreenShot("No Ad call made on other language Login page");
		} catch (AssertionError e) {
			generateFailReport("Ad call made on Other language Login page");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("AdsRegression.xls", "LoginPages");
	}

}
