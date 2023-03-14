package com.webmd.ads.regression;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

/**
 * @author sandeep.gowada
 * 
 *         PE-205245: Add URI to ad call across all pages on Medscape
 */

public class VerifyURIKeyValue extends AdsCommon {

	private static String consoleVariable = "window.location.pathname";
	String customParams;
	String uri;
	String consoleURI;

	@Test(dataProvider = "dataProvider", groups = { "testURIParameter" })
	public void verifyURICustParam(String url) {
		setDescription(
				" Verify that URI key-value pair is being passed in every ad call and that they  match the console value with URI in custom parameters & URL");
		getDriver();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			url = url.replace("medscape", env + "medscape");
		}
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		if (verifySpecificCallPresence(AdsConstantns.AD_CALL)) {
			validateURIkeyValue();
		}
		getServer().newHar();
		while (scrollTillNextLazyLoadFound()) {
			validateURIkeyValue();
		}
	}

	/*
	 * Method to validate the 'URI' key value pair
	 *
	 */
	private void validateURIkeyValue() {

		String currentURL = getDriver().getCurrentUrl();
		consoleURI = getConsoleValue(consoleVariable);
		generateInfoReport("Verifying whether current url matches with the console value of " + consoleVariable + "");
		Assert.assertTrue(currentURL.contains(consoleURI), "Fail: Current URL: " + currentURL
				+ " does not match with console value of " + consoleVariable + ": " + consoleURI);
		generatePassReport("Pass: Current URL matches with the console value of " + consoleVariable + " ");
		customParams = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.CUST_PARAMS.value());
		if (customParams != null) {
			uri = StringUtils.substringBetween(customParams, "&uri=", "&");
			if (uri != null && !(uri.isEmpty())) {
				if (uri.contains("%2F")) {
					uri = uri.replace("%2F", "/");
				}
				generateInfoReport("Verifying whether URI from custom params is equal to console value of "
						+ consoleVariable + "");
				try {
					Assert.assertEquals(uri, consoleURI);
					generatePassReport(
							"Pass: URI from custom params is equal to console value of " + consoleVariable + "");
				} catch (AssertionError e) {
					generateFailReport("Fail: URI in the custom parameters :" + uri
							+ " does not match the console value from " + consoleVariable + ": " + consoleURI);
				}

				generateInfoReport("Verifying whether current URL matches with the URI in custom parameters");
				try {
					Assert.assertTrue(currentURL.contains(uri));
					generatePassReport("Pass: Current URL matches with the URI in custom parameters");
				} catch (AssertionError e) {
					generateFailReport("Fail: Current URL: " + currentURL
							+ " does not contain the URI from custom parameters: " + uri);
				}

			} else {
				generateFailReport(
						"Fail: URI found null or empty in the custom parameters of ad call for URL: " + currentURL);
			}
		} else {
			generateInfoReport("Custom parameters found null");

		}

	}

	@DataProvider
	public String[] dataProvider() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}
}
