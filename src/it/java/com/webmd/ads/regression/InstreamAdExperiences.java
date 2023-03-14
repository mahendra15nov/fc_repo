package com.webmd.ads.regression;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;

/**
 * Created on 04/03/2019 Version : V 1.0;
 * 
 * Instream Ad Experiences On InfiniteSlide show
 * 
 * @author amahendra Updated on [22/07/2019] as per PPE-200937 and PPE-205841
 *         requirements
 */
@Listeners(com.webmd.general.common.Listener.class)
public class InstreamAdExperiences extends AdsCommon {

	JavascriptExecutor jse;
	String lazyLoadInt = "";
	int cSlides = 0;
	int nSlides = 1;
	int tSlides = 0;
	boolean lazyLoad = false;
	String texValue = "";
	String urlSL = " has not found.";
	boolean isInfiniteSlide = false;

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(alwaysRun = true)
	public void openBrowser() {
		login(getProperty("username"), getProperty("password"));
	}

	@AfterMethod(alwaysRun = true)
	public void resetValues() {
		lazyLoadInt = "";
		cSlides = 0;
		nSlides = 1;
		tSlides = 0;
		lazyLoad = false;
		texValue = "";
		urlSL = " has not found.";
		isInfiniteSlide = false;
	}

	/**
	 * Verify the Instream Ad exp in infinite slideshows.
	 * 
	 * @param url
	 */
	@Test(dataProvider = "medscapeurls", groups = { "InstreamAd", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyInstreamAdExp(String url) {
		getServer().newHar();
		if (breakPoint.equals("1")) {
			if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
				url = url.replace("medscape", env + "medscape");

			getURL(url);
			waitForAdCallFound();
			preRequesite();
			if (isInfiniteSlide) {
				// 1005 pos validation
				verify1005PosInAdCall();
				// No Refresh ad call
				verifyNoForceRefresh();
				// Instream Ad experience validation
				verifyInstreamAdExperience();
			} else {
				generateWarningReport(url + " is not a Infinite slide show.");
			}
		}
	}

	/**
	 * Verifies the pos 1005 in Ad call with 1x2 size
	 * 
	 * @param url
	 */
	public void verify1005PosInAdCall() {
		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				if (numberOfAdCallsValidation()) {
					if (getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.value())
							.contains(AdPosENUM.POS_1005.value())) {
						generatePassReportWithNoScreenShot(
								"pos " + AdPosENUM.POS_1005.value() + " is found in Ad call.");
						generateReport(getSizesForSpecificPositionFromAdCall(AdPosENUM.POS_1005.value()).equals("1x2"),
								"1x2 size is appears for pos 1005.",
								"1x2 size is not appears for pos " + AdPosENUM.POS_1005.value());

					} else {
						generateFailReport("pos " + AdPosENUM.POS_1005.value() + " is not found in Ad call.");
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
	 * Displays the current slid number
	 */
	private void servingSlide() {
		try {
			if (isVisibleInViewport(getDriver().findElement(
					By.xpath("//*[contains(@class,'crs-slide') and @data-slidenumber='" + nSlides + "']")))) {
				generateInfoReport("Currently we are viewing " + nSlides + " slide show page.");
				nSlides++;
			}
		} catch (NoSuchElementException e1) {
			generateFailReport("Failed in identifying the slide show.");
		}
	}

	/**
	 * Verify the 1122 pos in page laoded Ad call
	 */
	private void pos1122InPageLoadedAdCall() {
		if (numberOfAdCallsValidation()) {
			// verify texValeu
			verifytexValue(texValue);
			if (getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.value())
					.contains(AdPosENUM.POS_1122.value())) {
				generateFailReport("Ad call has been found in " + getDriver().getCurrentUrl());
			}
		} else {
			generatePassReportWithNoScreenShot("No Ad call has been found in " + getDriver().getCurrentUrl());
		}
	}

	/**
	 * PPE-191448 Mobile > Verify on pages with the First Ad Experience that an
	 * in stream pos 1122 ad appears after every 5th slide
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	public void verifyInstreamAdExperience() {

		servingSlide();
		// Verify the 1122 pos in Ad call
		pos1122InPageLoadedAdCall();

		int height = getDriver().manage().window().getSize().getHeight();
		int scroll = height / 250;
		int s = scroll;
		int max = 500;
		WebElement footer = null;
		try {
			footer = getDriver().findElement(By.xpath("// *[@id='footercontents']"));
		} catch (NoSuchElementException e) {
			max = 20;
		}
		int nAdCall = 0;
		for (int i = 0; i < max; i++) {
			if (nSlides <= tSlides) {
				// serving slide
				servingSlide();

				getServer().newHar();
				StaticWait(2);
				s = s + scroll;
				((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(0," + s + ")");
				if (numberOfAdCallsValidation()) {
					nAdCall++;
					if (nAdCall > 1) {
						posAndSize("", "", "", AdPosENUM.POS_1005.value());
					}
					// verify the 1122 pos in ad call.
					verifyPos1122();
					// verify texValeu
					verifytexValue(texValue);
					// verify the Instream Ad exp
					verifyInstreamAdExp();
					// verify pinline
					verifyPineline();
				}
				if (footer != null && isVisibleInViewport(footer)) {
					break;
				}
			} else {
				generateInfoReport("We are currently in last slide (" + tSlides + ").");
				break;
			}
		}
	}

	/**
	 * Prerequisite check list
	 * 
	 */
	private void preRequesite() {
		try {
			if (breakPoint.equalsIgnoreCase("1") && getDriver().getCurrentUrl().contains("slideshow") && getDriver()
					.findElement(By.xpath("//section[@class='crs-header infinitescroll-only']")).isDisplayed()) {
				isInfiniteSlide = true;
			}
		} catch (NoSuchElementException e) {
			generateFailReport("Failed in identifying the slide show.");
		}
		lazyLoadInt = getConsoleValue("slideShowVars.lazyLoadInt");
		List<WebElement> infiniteSlides = getDriver().findElements(By.xpath("//span[@class='crs-slide_pagination']"));
		tSlides = infiniteSlides.size();
		generateReport(tSlides >= 1, tSlides + " slides are exists in given url.",
				"There is only one / none slides in given " + getDriver().getCurrentUrl());

		if (!lazyLoadInt.isEmpty()) {
			cSlides = Integer.parseInt(lazyLoadInt);
			texValue = "cr2019_" + cSlides + "slides";
		} else {
			texValue = "";
			if (getDriver().getCurrentUrl().contains("ecd=conmkt")) {
				cSlides = 3;
			} else {
				cSlides = 5;
			}
		}

	}

	/**
	 * PPE-191457 Mobile > Verify that if tex=cr2019_5slides appears in ad call
	 * then pos 1122 should be exists and pos = 1005 should not be in ad call as
	 * well on page.
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	public void verifytexValue(String texValue) {
		// Verify the Key=Value1
		String custParam = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.CUST_PARAMS.value());
		if (custParam != null && !custParam.isEmpty()) {
			// First/default In stream Ad Experience
			if (texValue.isEmpty()) {
				generateReport(!custParam.contains("tex="), "tex key value is not apppears in Ad call.",
						"tex key value is apppears in Ad call.");
			} else {
				generateReport(custParam.contains(texValue),
						texValue + " has been found in the Ad call hence First In Stream Ad expeiance should be appear on the page.",
						texValue + " does not exists in the ad call but slideShowVars.lazyLoadInt value is ");
			}

		} else {
			generateFailReport("cust_param could not find in ad call.");
		}
	}

	/**
	 * PPE-191462 Mobile > Verify that pinlines above and below pos 1122 should
	 * be exists.
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	public void verifyPineline() {
		try {
			WebElement ele = getDriver()
					.findElement(By.xpath("//*[contains(@id,'instream') and @data-index='" + nSlides + "']"));
			if (ele.isDisplayed() && (nSlides % cSlides == 0)) {
				if (ele.getAttribute("class").contains("blank-ad")
						|| ele.getAttribute("style").contains("display: none;")) {
					generateInfoReport("1122 pos has serving the blank ad / display: none after slide " + nSlides);
				} else {
					if (ele.getAttribute("class").contains("instream-ad ad-loaded AdUnit")) {
						generatePassReportWithNoScreenShot("Pinelines are applied aboev and below of ad image.");
						generateReport(
								ele.getCssValue("border-top").contains("1px")
										&& ele.getCssValue("border-bottom").contains("1px"),
								"Pinelines applied with " + ele.getCssValue("border-top") + " at top and "
										+ ele.getCssValue("border-bottom") + " at bottom.",
								"Pinlines not applied properly.");

					} else {
						generateFailReport("Pinelines has not been added to the ad image.");
					}
				}
			} else {
				if (ele.isDisplayed())
					generateFailReport(
							"In stream Ad has been displayed before / after the expected slides by violating the first/second ad experience rules.");
			}
		} catch (NoSuchElementException ee) {
			StaticWait(3);
		}
	}

	/**
	 * PPE-191463 Mobile > Verify that there should not be forced refresh rule
	 * applied for pos 1122 ads.
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	public void verifyNoForceRefresh() {
		getServer().newHar();
		StaticWait(30);
		generateReport(!numberOfAdCallsValidation(), "No refresh ad call has not been found.",
				"Refresh ad call has been found.");
	}

	/**
	 * Verify the instream ad
	 */
	public void verifyInstreamAdExp() {

		try {
			WebElement ele = getDriver()
					.findElement(By.xpath("//*[contains(@id,'instream') and @data-index='" + nSlides + "']"));

			if (ele.isDisplayed() && (nSlides % cSlides == 0)) {
				String locator = "//*[contains(@id,'instream') and @data-index='" + nSlides + "']";
				if (verifySpecificPositionLoadedOnPage(AdPosENUM.POS_1122.value(), locator)) {
					WebElement iframe = getDriver().findElement(By.xpath(locator + "//iframe"));

					int iwidth = Integer.parseInt(iframe.getAttribute("width"));
					int iheight = Integer.parseInt(iframe.getAttribute("height"));
					// 300x250|300x400|300x50|300x51|320x50|320x51
					if ((iwidth == 300 || iwidth == 320)
							&& (iheight == 250 || iheight == 400 || iheight == 50 || iheight == 51)) {
						generatePassReportWithNoScreenShot(
								AdPosENUM.POS_1122.value() + " pos has been served with width : " + iwidth
										+ " and height : " + iheight + " on the page after slide " + nSlides);
					} else {
						generateFailReport(
								AdPosENUM.POS_1122.value() + " pos Ad has not been served after slide " + nSlides);
					}
				}

			} else {
				if (ele.isDisplayed())
					generateFailReport(
							"In stream Ad has been displayed before / after the expected slides by violating the instream ad experience rules.");
			}
		} catch (NoSuchElementException ee) {
			generateFailReport("Failed in identifying the slide.");
		}
	}

	/**
	 * Ad call validation, prev_scp and their sizes.
	 * 
	 * @param lazyLoad
	 *            - true - if it is lazy loaded ad call, false for page loaded
	 *            ad calls
	 */
	public void verifyPos1122() {
		// number of secure ads call verification
		// Verification of prev_scp and prev_iu_sizes
		String prevScp = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_SCP.value());
		String prevIUSzs = getSpecificKeyFromSecurePubadCall(QueryStringParamENUM.PREV_IU_SZS.value());
		generateInfoReport("prev_scp : " + prevScp);
		generateInfoReport("prev_iu_szs : " + prevIUSzs);
		if ((prevScp != null && (!prevScp.isEmpty())) && (prevIUSzs != null && (!prevIUSzs.isEmpty()))) {
			// pos 1122 verification in ad call
			if (prevScp.contains(AdPosENUM.POS_1122.value())) {
				generatePassReportWithNoScreenShot(
						"Ad pos-1122 has been found in ad call hence first / second Ad experience should be appear on the screen.");
				// applicable sizes verification
				String expectedPos = "1122";
				String unExpSize = "";
				String unExpectedPos = "";

				if (getDriver().getCurrentUrl().contains("ecd=conmkt")) {
					String[] esizes = { "300x250" };
					verifyApplicableSizes(esizes, expectedPos, unExpSize, unExpectedPos);
				} else {
					String[] esizes = { "300x250", "300x400", "300x50", "300x51", "320x50", "320x51" };
					verifyApplicableSizes(esizes, expectedPos, unExpSize, unExpectedPos);
				}

			}
		} else {
			generateFailReport("prev_scp / prev_iu_szs has been found with null/empty in the Ad call.");
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://www.medscape.com/slideshow/uk-doctors-salary-report-6009730",
				"https://www.medscape.com/slideshow/uk-doctors-salary-report-6009730?ecd=conmkt",
				"https://www.medscape.com/slideshow/uk-doctors-satisfaction-survey-6009772",
				"https://www.medscape.com/slideshow/uk-doctors-satisfaction-survey-6009772?ecd=conmkt",
				"https://www.medscape.com/slideshow/2018-compensation-overview-6009667",
				"https://www.medscape.com/slideshow/2019-compensation-overview-6011286",
				"https://www.medscape.com/slideshow/2018-compensation-overview-6009667",
				"https://www.medscape.com/slideshow/2018-young-physician-compensation-6009915",
				"https://www.medscape.com/slideshow/2018-compensation-anesthesiologist-6009650",
				"https://www.medscape.com/slideshow/2019-compensation-psychiatrist-6011346",
				"https://www.medscape.com/slideshow/compensation-2017-overview-6008547",
				"https://www.medscape.com/slideshow/2019-compensation-overview-6011286",
				"https://www.medscape.com/slideshow/2019-compensation-internist-6011334" };
	}
}
