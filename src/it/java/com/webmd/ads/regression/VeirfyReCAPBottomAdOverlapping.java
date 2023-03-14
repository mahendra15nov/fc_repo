package com.webmd.ads.regression;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * PPE-205764 ReCAP - bottom ad overlapping content
 */

public class VeirfyReCAPBottomAdOverlapping extends AdsCommon {

	private static final String[] Adpos122 = { "300x250", "300x600", "300x350" };
	private static final String[] Adpos141 = { "728x90", "728x91" };

	@Test(dataProvider = "dataProvider", groups = { "Desktop" })
	public void test(String url) {
		url = url.replace("env.", env);
		setDescription(
				"Verify that on visiting any recap page, the right rail ad pos 910 does not overrun into the container of ad pos 141 in the footer");
		getDriver();
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		checkAdSizes("122", Adpos122);
		getServer().newHar();
		if (scrollTillNextLazyLoadFound()) {
			generateInfoReport("Executing galen to check if right rail ad does not overrun into the footer");
			call_Gallen("TestInput/GalenSpecs/RecapPagesAdOverrun.gspec", url);
			checkAdSizes("141", Adpos141);
		} else {
			generateFailReport("Lazy loaded ad call not recorded. Pos 141 not loaded");
		}

	}

	private void checkAdSizes(String pos, String[] expected) {
		String[] size = getSizesForSpecificPositionFromAdCall(pos).split("\\|");
		if (size.length == expected.length) {
			for (String temp : expected) {
				if (Arrays.asList(size).contains(temp)) {
					generatePassReport("Test passed for Ad pos " + pos + " with presence of size : " + temp
							+ " in expected sizes: " + Arrays.toString(expected));
				} else {
					generateFailReport("Test failed for Ad pos " + pos + " with presence of size : " + temp
							+ " in expected sizes: " + Arrays.toString(expected));
				}
			}

		} else {
			generateFailReport("Fail: Size of Ad pos " + pos + " is not correct. Expected: " + Arrays.toString(expected)
					+ " but found " + Arrays.toString(size) + "");
		}
	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://reference.env.medscape.com/recap/896529" },
				{ "https://reference.env.medscape.com/recap/897025" } };
	}
}
