package com.webmd.ads;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * Created on 21/11/2019 Version : V 1.0;
 * 
 * PPE-223707 - MFR Unit - Create a DFP Native Template for Manufacturing
 * Services Unit
 * 
 * @author amahendra
 */
@Listeners(com.webmd.general.common.Listener.class)
public class VerifyManufacturingServicesUnit extends AdsCommon {

	String mfrPosValue = null;

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(alwaysRun = true)
	public void openBrowser() {
		login(getProperty("username"), getProperty("password"));
		jse = (JavascriptExecutor) getDriver();
		mfrPosValue = breakPoint.equals("1") ? "2017" : "421";
	}

	/**
	 * PPE-223707 - MFR Unit - Create a DFP Native Template for Manufacturing
	 * Services Unit
	 * 
	 * @param url
	 */
	@Test(dataProvider = "medscapeurls", groups = { "MFU", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyMFUnit(String url) {
		By mfrPos = By.xpath("//*[@id='ads-pos-" + mfrPosValue + "']");
		By mfrIframe = By.xpath("//*[@id='ads-pos-" + mfrPosValue + "']//iframe");
		getServer().newHar();
		getURL(url);
		waitForAdCallFound();
		StaticWait(10);
		if (!is404(getDriver().getTitle()) && !isLoginPage()) {
			if (numberOfAdCallsValidation()) {

				if (verifySpecificAdPresenceInSecurePubadCall(mfrPosValue)) {
					generatePassReportWithNoScreenShot("MFR Pos " + mfrPosValue + " is appears in Ad call.");
				}

				WebElement divMfr = getDriver().findElement(mfrPos);
				if (divMfr.isDisplayed()) {
					generatePassReportWithNoScreenShot("MFR Unit div has been loaded on the page.");
					if (breakPoint.equals("1")) {

						String borderTop = divMfr.getCssValue("border-top");
						borderTop = StringUtils.substringBefore(borderTop, "rgb")
								+ getRGBAColorCodeInHexCode(borderTop.substring(borderTop.indexOf("rgb")));
						generateReport(borderTop.equals("1px solid #d8d8d8"), "border-top is '1px solid #d8d8d8'",
								"border-top is '" + borderTop + "'");

						String borderBottom = divMfr.getCssValue("border-bottom");
						borderBottom = StringUtils.substringBefore(borderBottom, "rgb")
								+ getRGBAColorCodeInHexCode(borderBottom.substring(borderBottom.indexOf("rgb")));
						generateReport(borderBottom.equals("1px solid #d8d8d8"), "border-top is '1px solid #d8d8d8'",
								"border-top is '" + borderBottom + "'");

					} else {

						generateReport(divMfr.getCssValue("background-image").equals("none"),
								"background-image is 'none'",
								"background-image is '" + divMfr.getCssValue("background-image") + "'");
						generateReport(divMfr.getCssValue("padding-top").equals("10px"), "padding-top is '10px'",
								"padding-top is '" + divMfr.getCssValue("padding-top") + "'");
						generateReport(divMfr.getCssValue("padding-bottom").equals("0px"), "padding-bottom is '0px'",
								"padding-bottom is '" + divMfr.getCssValue("padding-bottom") + "'");

					}

					if (getDriver().findElement(mfrIframe).isDisplayed()) {
						generatePassReportWithNoScreenShot("MFR Unit iframe has been loaded on the page.");
						generateReport(divMfr.getCssValue("min-width").equals("0px"), "min-width is '0px'",
								"min-width is '" + divMfr.getCssValue("min-width") + "'");
					} else {
						generateFailReport("MFR Unit iframe has not been loaded on the page.");
					}
				} else {
					generateFailReport("MFR Unit div has not been loaded on the page.");
				}
			} else {
				generateInfoReport("Ad call has not been found in page load.");
			}
		} else {
			generateSkipReport(getDriver().getCurrentUrl() + " is not found in " + env);
		}

	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] {
				"https://reference.medscape.com/drug/erivedge-vismodegib-999716?google_preview=i-H19J81T2gY6viu7gUw6pTk9QWIAYCAgKCnivDjGQ&iu=4312434&gdfp_req=1&lineItemId=4907050310&creativeId=138221209795" };
	}
}
