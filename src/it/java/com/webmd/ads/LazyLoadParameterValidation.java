package com.webmd.ads;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

public class LazyLoadParameterValidation extends AdsCommon {

	@BeforeClass(groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void beforeClass() {
		getDriver();
		login(getProperty("username"), getProperty("password"));
	}

	@AfterClass(groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}
	// This method will validate whether lazyloaded value passed is tracked
	// under cust_params or not

	private boolean validateLazyLoadValue(String expected) {

		boolean flag = false;
		Har har = getServer().getHar();
		har.getLog().getBrowser();
		List<HarEntry> res = har.getLog().getEntries();

		for (HarEntry harEntry : res) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("securepubads.g.doubleclick.net/gampad/ads?")) {
				flag = true;
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().equals("cust_params")) {
						if (harNameValuePair.getValue().contains(expected)) {
							generatePassReport("Lazyload value tracked properly");
							if (StringUtils.countMatches(harNameValuePair.getValue(), expected) > 1)
								generateFailReport("ll key and value tracked more than once");
						} else {
							generateFailReport("Lazyload value tracked wrong \n" + harNameValuePair.getValue());
						}
					}
				}
			}
		}
		return flag;
	}

	@Test(dataProvider = "dataProvider", groups = { "AdsRegression", "Desktop", "MobileWeb" })
	public void testLazyLoadParameter(String URL) {
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			URL = URL.replace("medscape", env + "medscape");

		getURL(URL);
		waitForAdCallFound();
		if (isLoginPage()) {
			login(getProperty("username"), getProperty("password"));
		}
		if (!isLoginPage()) {
			if (isTrue("_isAnArticle")) {
				generateInfoReport("Validating page load");
				if (!validateLazyLoadValue("ll=0"))
					generateFailReport("No Ad call observed for Pageload");
				getServer().newHar();
				scrollTillEnd();
				StaticWait(5);
				generateInfoReport("Validating Lazyload");
				if (!validateLazyLoadValue("ll=1"))
					generateInfoReport("No Lazyload Ad call shown on page");
			} else {
				generateSkipReport(URL + " is not a article.");
			}
		} else {
			generateSkipReport(URL + " is required login.");
		}
	}

	@DataProvider
	public String[] dataProvider() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}

}
