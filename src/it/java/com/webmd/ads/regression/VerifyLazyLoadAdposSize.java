package com.webmd.ads.regression;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * PPE-191326 Incorrect ad sizes requested out of lazy-loaded position 122 on
 * desktop
 */
public class VerifyLazyLoadAdposSize extends AdsCommon {
	private static final String[] ADPOS_122 = { "300x600", "300x250" };
	private static final String[] ADPOS_910 = { "300x250", "300x251" };

	@Test(dataProvider = "dataProvider", groups = { "Desktop" })
	public void validateAdposSize(String url) {
		url = url.replace("env.", env);
		setDescription("Verify whether ad position 122 is firing the correct size: " + ADPOS_122
				+ " and position 910 is firing the size " + ADPOS_910 + " for lazy loaded ad call");
		getDriver();
		login("infosession33", "medscape");
		getDriver().get(url);
		getServer().newHar();
		generateInfoReport("Checking the Ad position sizes for lazy loaded ad calls for url: " + url);
		while (scrollTillNextLazyLoadFound()) {
			if (verifySpecificAdPresenceInSecurePubadCall("122")) {
				checkAdSizes("122", ADPOS_122);
			}
			if (verifySpecificAdPresenceInSecurePubadCall("910")) {
				checkAdSizes("910", ADPOS_910);
			}
		}
	}

	private void checkAdSizes(String pos, String[] expected) {
		String[] size = getSizesForSpecificPositionFromAdCall(pos).split("\\|");
		if (size.length == expected.length) {
			for (String temp : expected) {
				if (Arrays.asList(size).contains(temp)) {
					generatePassReport("Test passed for Ad pos " + pos + " with presence of size : "
							+ Arrays.toString(size) + " in expected sizes: " + Arrays.toString(expected));
				} else {
					generateFailReport("Test failed for Ad pos " + pos + " with presence of size : "
							+ Arrays.toString(size) + " in expected sizes: " + Arrays.toString(expected));
				}
			}

		} else {
			generateFailReport("Fail: Size of Ad pos " + pos + " is not correct. Expected: " + Arrays.toString(expected)
					+ " but found " + Arrays.toString(size) + "");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://emedicine.env.medscape.com/article/116467-clinical#b5" },
				{ "https://reference.env.medscape.com/drug/coumadin-jantoven-warfarin-342182" },
				{ "https://emedicine.env.medscape.com/article/1134817-overview" },
				{ "https://reference.env.medscape.com/drug/imbruvica-ibrutinib-999896" },
				{ "https://emedicine.env.medscape.com/article/1344081-overview" } };
	}
}
