package com.webmd.ads;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.Cookie;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class RemovePageFair extends AdsCommon {

	public static boolean omnitureFlag = true, cookieFlag = true;
	public String[][] input;

	@BeforeMethod()
	public void beforeMethod() {
		getDriver();
		// login();
		input = XlRead.fetchDataExcludingFirstRow("TestData/iuPartsTest.xls", "pageFair");
	}

	@AfterClass()
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	private void verifyOmniture() {
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("ssl")) {
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().equalsIgnoreCase("mmodule")) {
						if (harNameValuePair.getValue().equalsIgnoreCase("pagefair")) {
							omnitureFlag = false;
							break;
						}
					}
				}
			}
		}
	}

	private void verifyCookie() {
		Set<Cookie> cookies = getDriver().manage().getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase("gab")) {
				cookieFlag = false;
				break;
			}
		}
	}

	@Test() // (dataProvider = "dataProvider")
	public void test() {

		for (int i = 0; i < input.length; i++) {
			getServer().newHar();
			getDriver().get(input[i][1]);
			generateInfoReport("Verifying for " + input[i][1]);
			verifyOmniture();
			verifyCookie();
		}
		try {
			Assert.assertTrue(cookieFlag);
			generatePassReport("No Cookie observed with gab");
		} catch (AssertionError e) {
			generateFailReport("Cookie observed with gab");
		}

		try {
			Assert.assertTrue(omnitureFlag);
			generatePassReport("No Omniture acll has mmodule value as pagefair");
		} catch (AssertionError e) {
			generateFailReport("Omniture acll has mmodule value as pagefair");
		}
	}

}
