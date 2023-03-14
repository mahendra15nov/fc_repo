package com.webmd.ads.regression;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

/**
 * @author sandeep.gowada
 *
 *         PPE-178411: Pass al=0 if when al=null
 *
 */

public class VerifyParameterValue extends AdsCommon {
	String customParameters;
	String alParameter;

	@Test(dataProvider = "dataProvider", groups = { "Desktop", "MobileWeb" })
	public void verifyalparameter(String url) {
		setDescription("Verify whether 'al' parameter is loaded with some value (even 0) and should not be null");
		getDriver();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			url = url.replace("medscape", env + "medscape");
		}
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		if (verifySpecificCallPresence(AdsConstantns.AD_CALL)) {
			checkParamValue("Page-Loaded");
		} else {
			generateInfoReport("Page-Loaded Ad call is not recorded for url: " + getDriver().getCurrentUrl());
		}
		getServer().newHar();
		while (scrollTillNextLazyLoadFound()) {
			checkParamValue("Lazy-Loaded");
		}
	}

	/**
	 * Method to check the 'al' parameter for not null and non empty. Generates pass
	 * report if parameter has a value. Generates fail report if parameter not
	 * found/null/empty
	 *
	 */
	private void checkParamValue(String adType) {
		generateInfoReport("Verifying 'al' parameter for " + adType + " Ad call");
		customParameters = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.CUST_PARAMS.value());
		alParameter = StringUtils.substringBetween(customParameters, "&al=", "&");
		generateInfoReport("'al' parameter in " + adType + " ad call found as: " + alParameter);
		if (alParameter != null && !(alParameter.isEmpty())) {
			generatePassReport("Pass: al parameter is verified for not null and non empty for " + adType + " ad call");
		} else {
			generateFailReport(
					"Fail: al parameter found null in " + adType + " ad call for url: " + getDriver().getCurrentUrl());
		}
	}

	@DataProvider
	public String[] dataProvider() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}

}
