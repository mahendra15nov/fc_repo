package com.webmd.ads.regression;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * @author sandeep.gowada
 *
 */
public class VerifyRecapAdSizesMobileWeb extends AdsCommon {

	/**
	 * PPE-193034 ReCAP - different ad sizes not being passed
	 */
	@Test(dataProvider = "dataProvider", groups = { "MobileWeb" })
	public void verifyAdSizeMobileWeb(String url) {
		url = url.replace("env.", env);
		setDescription(
				"Verify whether POS: 1122 has SIZE: 300x250|300x400|300x50|300x51|320x50|320x51 and POS: 1005 has SIZE: 1x2");
		getDriver();
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		String prevScpValues = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.value());
		generateBoldReport("PREV_SCP VALUES");
		generateInfoReport("prev_scp=" + prevScpValues);
		String prevIuSzs = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_IU_SZS.value());
		generateBoldReport("PREV_IU_SZS");
		generateInfoReport("prev_iu_szs=" + prevIuSzs);
		String[] adPositions = prevScpValues.split("\\|");
		String[] adSizes = prevIuSzs.split(",");
		String[] expectedSize = ArrayUtils.EMPTY_STRING_ARRAY;
		if (adPositions != null && adSizes != null && (adPositions.length == adSizes.length)) {
			for (int i = 0; i < adPositions.length; i++) {
				String positions = StringUtils.substringBetween(adPositions[i], "pos=", "&");
				if (positions.contains("1122")) {
					expectedSize = new String[] { "300x250", "300x400", "300x50", "300x51", "320x50", "320x51" };
					String[] size = adSizes[i].split("\\|");
					verifyEquality(size, expectedSize, positions);
				}
				if (positions.contains("1005")) {
					String[] size = adSizes[i].split("\\|");
					expectedSize = new String[] { "1x2" };
					verifyEquality(size, expectedSize, positions);

				}
			}
		}

	}

	private void verifyEquality(String[] size, String[] expectedSize, String positions) {
		if (Arrays.equals(size, expectedSize)) {
			generatePassReport("PASS: Test passed for ad-size validation for position: " + positions
					+ "with expected sizes:" + Arrays.toString(expectedSize));
		} else {
			generateFailReport("FAIL: Test failed for ad-size validation for position: " + positions
					+ "with expected sizes:" + Arrays.toString(expectedSize));
		}

	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://reference.env.medscape.com/recap/908646" } };
	}
}
