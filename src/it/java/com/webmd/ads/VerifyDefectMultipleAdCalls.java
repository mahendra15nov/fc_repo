package com.webmd.ads;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * @author sandeep.gowada
 * 
 *         PPE-225087 MobileWeb: Multiple unviewable ad calls made on article
 *         pages when user not logged in
 *
 */
public class VerifyDefectMultipleAdCalls extends AdsCommon {

	@Test(dataProvider = "dataProvider", groups = { "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyMultipleAdCalls(String url) {

		if (breakPoint.equals("1")) {
			getDriver().manage().deleteAllCookies();
			getServer().newHar();
			if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
				url = url.replace("medscape", env + "medscape");
			}
			url = url + "?faf=1";
			getDriver().get(url);
			boolean result = numberOfAdCallsValidation();
			generateReport(result, "Validated if there are multiple ad calls loaded for anonymous user",
					"No ad call recorded for URL");

		} else {
			generateInfoReport("This test is intented only for Mobile breakpoint");
		}
	}

	@DataProvider
	public String[] dataProvider() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}
}
