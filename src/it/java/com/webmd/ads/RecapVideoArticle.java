package com.webmd.ads;

import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;

/**
 * Created on 21/08/2019 Version : V 1.0;
 * 
 * Recap Video Article
 * 
 * @author amahendra Created on [21/08/2019] - PPE-209593 - POC: Update ReCAP
 *         Video pages to include new position to serve Video IFI
 */
@Listeners(com.webmd.general.common.Listener.class)
public class RecapVideoArticle extends AdsCommon {

	JavascriptExecutor jse;
	boolean lazyLoad = false;
	String urlSL = " has not found.";

	@AfterClass(groups = { "Recap", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(groups = { "Recap", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void openBrowser() {
		login(getProperty("username"), getProperty("password"));
	}

	@AfterMethod(groups = { "Recap", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void resetValues() {
		lazyLoad = false;
		urlSL = " has not found.";
	}

	/**
	 * Verify the Instream Ad exp in infinite slideshows.
	 * 
	 * @param url
	 */
	@Test(enabled = true, dataProvider = "medscapeurls", groups = { "Recap", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyMDPAdExp(String url) {
		getServer().newHar();
		getURL(url);
		waitForAdCallFound();
		// 101 pos validation
		verify800PosInAdCall();
	}

	/**
	 * Verifies the pos 800 in Ad call with 2x10 size
	 * 
	 * @param url
	 */
	public void verify800PosInAdCall() {
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				if (numberOfAdCallsValidation()) {
					if (getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.value())
							.contains(AdPosENUM.POS_800.value())) {
						generatePassReportWithNoScreenShot(
								"pos " + AdPosENUM.POS_800.value() + " is found in Ad call.");
						// Sizes verification
						String[] esizes = { "2x10" };
						verifySzs(AdPosENUM.POS_800.value(), esizes);
						// Verify 101 div on the page
						verifySpecificPositionLoadedOnPage(AdPosENUM.POS_800.value());

					} else {
						generateFailReport("pos " + AdPosENUM.POS_800.value() + " is not found in Ad call.");
					}
				} else {
					generateInfoReport("Ad call has not been found in page load.");
				}
			}
		} else {
			generateSkipReport(getDriver().getCurrentUrl() + urlSL);
		}
	}

	/**
	 * Ad call validation, prev_scp and their sizes.
	 * 
	 * @param lazyLoad
	 *            - true - if it is lazy loaded ad call, false for page loaded
	 *            ad calls
	 */
	public void verifySzs(String pos, String[] esizes) {
		// number of secure ads call verification
		// Verification of prev_scp and prev_iu_sizes
		String prevScp = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.value());
		String prevIUSzs = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_IU_SZS.value());
		generateInfoReport("prev_scp : " + prevScp);
		generateInfoReport("prev_iu_szs : " + prevIUSzs);
		if ((prevScp != null && (!prevScp.isEmpty())) && (prevIUSzs != null && (!prevIUSzs.isEmpty()))) {
			// pos 101 verification in ad call
			if (prevScp.contains(pos)) {
				generatePassReportWithNoScreenShot("Ad pos-" + pos + " has been found in ad call.");
				// applicable sizes verification
				String unExpSize = "";
				String unExpectedPos = "";
				verifyApplicableSizes(esizes, pos, unExpSize, unExpectedPos);
			}
		} else {
			generateFailReport("prev_scp / prev_iu_szs has been found with null/empty in the Ad call.");
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://reference.qa01.medscape.com/recap/896529" };
	}
}
