package com.webmd.ads.regression;

import java.util.List;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class ValidateDuplicate141AdCall extends AdsCommon {

	static int adCountOnPage, adCountOnCall;
	By asPos = By.xpath("//div[contains(@id,'ads-pos-141')]");

	private void updateAdCountOnPage() {
		adCountOnPage = getDriver().findElements(asPos).size();
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		login("raruva@webmd.net", "medscape");
	}

	@AfterClass(alwaysRun = true)
	public void quitBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	private void updateAdCountOnCall() {
		System.out.println("from verifySpecificAdPresenceInSecurePubadCall");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("securepubads.g.doubleclick.net/gampad/ads?")) {
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().equals("prev_scp")) {
						generateInfoReport(harNameValuePair.getValue());
						if (harNameValuePair.getValue().contains("141"))
							adCountOnCall++;
					}
				}
			}
		}
	}

	@Test(dataProvider = "dataProvider")
	public void validateDuplicateAdPos(String type, String URL) {
		generateInfoReport("Validating URL of Type: " + type);
		adCountOnPage = 0;
		adCountOnCall = 0;
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			URL = URL.replace("medscape", env + "medscape");
		}
		getServer().newHar();
		getDriver().get(URL);
		scrollTillEnd();
		scrollBottomToTop();
		updateAdCountOnPage();
		updateAdCountOnCall();

		if (!(adCountOnCall == 0 && adCountOnPage != 0)) {
			try {
				Assert.assertEquals(adCountOnPage, adCountOnCall);
				generatePassReport("141 ad call count match with ads on page");
			} catch (AssertionError e) {
				generateFailReport(
						"On page " + adCountOnPage + " ads loaded, On Ad calls " + adCountOnCall + " ads loaded");
			}
		} else {
			try {
				Assert.assertEquals(getDriver().findElement(asPos).getAttribute("style"), "display: none;");
			} catch (AssertionError e) {
				generateFailReport(
						"On page " + adCountOnPage + " ads loaded, On Ad calls " + adCountOnCall + " ads loaded");
			}
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/qa01.xls", "Sheet1");
		/*
		 * return new String[][] {
		 * {"https://reference.dev01.medscape.com?faf=1"},
		 * {"https://www.dev01.medscape.com/pediatrics?faf=1"},
		 * {"https://www.dev01.medscape.com/cardiology?faf=1"},
		 * {"https://www.dev01.medscape.com/viewarticle/895088?faf=1"} };
		 */
	}

}
