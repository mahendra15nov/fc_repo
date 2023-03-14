package com.webmd.ads;

import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;

@Listeners(com.webmd.general.common.Listener.class)
public class FFQPagesValidation extends AdsCommon {

	JavascriptExecutor jse;

	@AfterClass(groups = { "FFQ" })
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(groups = { "FFQ" })
	public void login() {
		login(getProperty("username"), getProperty("password"));
	}

	@BeforeClass(groups = { "FFQ" })
	public void resetValues() {
		login(getProperty("username"), getProperty("password"));
		jse = (JavascriptExecutor) getDriver();
	}

	@Test(dataProvider = "medscapeurls", groups = { "FFQ" })
	public void VerifyNewAdhesiveFooterPosAndsApplicablesSizesInAdCall(String URL) throws InterruptedException {

		System.out.println(URL);
		getServer().newHar();
		getDriver().get(URL);

		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				adCallVerificationInFFQPages();
			} else {
				generateFailReport("Login required for " + URL);
			}
		} else {
			generateWarningReport(URL + " not found.");
		}

	}

	private void adCallVerificationInFFQPages() {
		// Page loaded Ad call validation
		if (numberOfAdCallsValidation()) {
			adcallvalidation();
		} else {
			if (!(getDriver().getCurrentUrl().contains("medscape.com/academy/business")
					|| getDriver().getCurrentUrl().contains("medscape.com/consult")
					|| getDriver().getCurrentUrl().contains("medscape.com/video")))
				generateFailReport("No Ad call has been found inn " + getDriver().getCurrentUrl());
			else
				generatePassReportWithNoScreenShot("No ad call has been found in academy/consult/video");
		}
		
	}

	/**
	 * Ad call validation, prev_scp and thir sizes.
	 * 
	 * @param pageLoad
	 *            - true - if it is page loaded ad call, false for lazy loaded
	 *            ad calls
	 */
	public void adcallvalidation() {
		// number of secure peburd call verification
		// Verification of prev_scp and prev_iu_sizes
		String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
		String prev_iu_szs = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
		generateInfoReport("prev_scp : " + prev_scp);
		generateInfoReport("prev_iu_szs : " + prev_iu_szs);
		if ((prev_scp != null && (!prev_scp.isEmpty())) && (prev_iu_szs != null && (!prev_iu_szs.isEmpty()))) {

			if (breakPoint.equalsIgnoreCase("4")) {
				// pos 145 verification in ad call
				if (StringUtils.countMatches(prev_scp, "909") > 1) {
					generateFailReport("More than 1909/909 positions are found in Ad call, prev_scp : " + prev_scp);
				} else {
					if (StringUtils.countMatches(prev_scp, "909") == 1)
						generatePassReportWithNoScreenShot("Only one 909/1909 position has beeen found in Ad call.");
					else
						generateFailReport(StringUtils.countMatches(prev_scp, "909")
								+ " 909/1909 positions has beeen found in Ad call.");
				}
				if (prev_scp.contains("909")) {
					generatePassReportWithNoScreenShot("Ad pos-909 has been found in ad call.");
					// applicable sizes verification
					String expectedPos = "909";
					String unExpSize = "";
					String expectedSize = "320x50";
					posAndSize(expectedSize, unExpSize, expectedPos, prev_scp, prev_iu_szs);
					expectedSize = "2x5";
					posAndSize(expectedSize, unExpSize, expectedPos, prev_scp, prev_iu_szs);

					// Verifying that whether duplicate sizes are exists
					String sizes = getSizesForSpecificPositionFromAdCall("909");
					if (sizes.isEmpty()) {
						generateFailReport("909 position is not available in Ad call.");
					} else {
						findDuplicateSizes(sizes);
					}
					// verification of extra sizes which are available
					// in the list
					String[] esizes = { "320x50", "2x5" };
					notApplicableSizesValidation(expectedPos, esizes);
				} else {
					generateFailReport("Ad pos-909 has not been found in ad call.");
				}

			} else {
				if (breakPoint.equalsIgnoreCase("1")) {
					// pos 1145 verification in ad call
					if (prev_scp.contains("1909")) {
						generateFailReport("Ad pos-1909 has been found in page loaded ad call.");
					} else {
						generatePassReportWithNoScreenShot("Ad pos-1909 has not been found in ad call.");
					}
				}
			}

		} else {
			generateFailReport("prev_scp / prev_iu_szs has been found with null/empty in the Ad call.");
		}
	}

	/**
	 * pos and applicable sizes validation.
	 * 
	 * @param expectedSize
	 *            - expected size
	 * @param unExpSize
	 *            - un expected size which should not be in the applicable sizes
	 *            list
	 * @param expectedPos
	 *            - expected position to be available in ad call.
	 * @param prev_scp
	 *            - prev_scp of ad call.
	 * @param prev_iu_szs
	 *            - ad call applicable sizes list.
	 */
	public void posAndSize(String expectedSize, String unExpSize, String expectedPos, String prev_scp,
			String prev_iu_szs) {
		StringTokenizer aPrev_scp = new StringTokenizer(prev_scp, "|");
		StringTokenizer aPrev_iu_szs = new StringTokenizer(prev_iu_szs, ",");

		if (aPrev_scp.countTokens() == aPrev_iu_szs.countTokens()) {

			while (aPrev_scp.hasMoreTokens()) {
				String position = "";
				String sizes = "";
				position = aPrev_scp.nextToken();
				sizes = aPrev_iu_szs.nextToken();
				generateInfoReport(position);
				generateInfoReport(sizes);
				if (position.contains(expectedPos))
					validatePositionAndSize(position, sizes, expectedPos, expectedSize, unExpSize);
			}
		} else {
			generateFailReport("prev_scp and prev_iu_szs counts are miss match, prev_scp count is "
					+ aPrev_scp.countTokens() + ", prev_iu_szs count is " + aPrev_iu_szs.countTokens());
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://reference.staging.medscape.com/viewarticle/885449",
				"https://reference.staging.medscape.com/viewarticle/894719",
				"https://reference.staging.medscape.com/viewarticle/894565",
				"https://reference.staging.medscape.com/viewarticle/894410",
				"https://reference.staging.medscape.com/viewarticle/879288",
				"https://reference.staging.medscape.com/viewarticle/893861" };
	}
}
