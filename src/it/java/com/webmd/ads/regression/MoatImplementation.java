package com.webmd.ads.regression;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;

/**
 * Created on 04/09/2019 Version : V 1.0;
 * 
 * Its Moat Implementation
 * 
 * @author amahendra
 *
 */

@Listeners(com.webmd.general.common.Listener.class)
public class MoatImplementation extends AdsCommon {

	JavascriptExecutor jse;
	boolean lazyLoad = false;
	boolean isMoatcallFound = false;
	int height;
	int scroll;
	int s;
	int max;

	@AfterTest(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
	}

	/**
	 * PPE-193126 Verify across all page types including language editions on
	 * Medscape that the Moat code is firing
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	@Test(dataProvider = "medscapeurls", groups = { "Moat", "AdsSmoke", "AdsRegression", "Desktop", "MobileWeb" })
	public void VerifyMoat(String URL) throws InterruptedException {
		isMoatcallFound = false;

		jse = (JavascriptExecutor) getDriver();
		login(getProperty("username"), getProperty("password"));
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			URL = URL.replace("medscape", env + "medscape");

		getURL(URL);

		if (!is404(getDriver().getTitle())) {
			if (!isLoginPage()) {
				// started the timer
				long startTime = System.currentTimeMillis();
				generateInfoReport("Count down started for Moat Service...");

				// waiting for Moat service returned
				while (!verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
					if ((System.currentTimeMillis() - startTime) >= 500)
						break;
				}
				verifyMoatServiceValueInAdCall(startTime);
				verifyMoatServiceValueInLazyLoadedAdCall(startTime);
				moat();
			} else {
				generateFailReport("Login required for " + URL);
			}
		} else {
			generateSkipReport(URL + " not found.");
		}
	}

	/**
	 * PPE-193145 Verify across all page types on Medscape including language
	 * editions that m_mv, m_gv, m_data, m_safety, m_categories key value pairs
	 * should be available in page loaded ad call when Moat service returned
	 * within 500 ms.
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	public void verifyMoatServiceValueInAdCall(long startTime) {

		// verification moat service call
		if (verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
			generatePassReportWithNoScreenShot("Maot server has been returned within 500ms.");

			// Waiting for Ad call
			while (!verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?")) {
				StaticWait(1);
				if ((System.currentTimeMillis() - startTime) >= 180000)
					break;
			}
			// verification of ad call
			if (verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?")) {
				generatePassReportWithNoScreenShot("Ad call has been found.");
			} else {
				generateFailReport("Ad call has not been found for " + getDriver().getCurrentUrl());
			}
			// Moat Values verification
			moatValuesVerification(true);

		} else {
			generateFailReport("Maot server has not been returned within 500ms.");
		}
	}

	/**
	 * PPE-193147 Verify that m_mv, m_gv, m_data, m_safety, m_categories key
	 * values pairs should be available in lazy loaded ad call when Moat service
	 * returned after 500 ms.
	 * 
	 * @param URL
	 * @throws InterruptedException
	 */
	public void verifyMoatServiceValueInLazyLoadedAdCall(long startTime) {

		// waiting for Moat service returned
		while (!verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
			if ((System.currentTimeMillis() - startTime) >= 500)
				break;
		}
		// verification moat service call
		if (verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
			isMoatcallFound = true;
			generatePassReportWithNoScreenShot("Maot server has been returned within 500ms.");
		} else {
			generateInfoReport("Maot server has not been returned within 500ms.");
			// Verify the Moat value when Moat has not returned the value
			moatValuesVerification(false);

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
			for (int i = 0; i < max; i++) {
				getServer().newHar();
				StaticWait(2);
				s = s + scroll;
				jse.executeScript("window.scrollBy(0," + s + ")");
				if (verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
					generatePassReportWithNoScreenShot("Moat service has been tracked in lazy loaded netwrok calls");
					isMoatcallFound = true;
					// Moat Values verification
					moatValuesVerification(true);
					break;
				}
				if (footer != null) {
					if (isVisibleInViewport(footer)) {
						break;
					}
				}
			}
		}
	}

	private void moat() {
		if (verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
			generatePassReportWithNoScreenShot("Moat service call has been tracked in page loaded network calls.");
			isMoatcallFound = true;
		} else {
			generateInfoReport("Moat service call has not been tracked in page loaded network calls.");
		}
		// Lazy load call verification
		if (!isMoatcallFound) {
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
			for (int i = 0; i < max; i++) {
				getServer().newHar();
				StaticWait(2);
				s = s + scroll;
				jse.executeScript("window.scrollBy(0," + s + ")");
				if (verifySpecificCallPresence("https://z.moatads.com/medscapeprebidheader397676338454")) {
					isMoatcallFound = true;
					break;
				}
				if (footer != null) {
					if (isVisibleInViewport(footer)) {
						break;
					}
				}
			}
			if (isMoatcallFound) {
				generatePassReportWithNoScreenShot(
						"Moat service call has been tracked in page loaded / Lazy Loaded network calls.");
			} else {
				generateFailReport(
						"Moat service call has not been tracked in page loaded / Lazy loaded network calls.");
			}
		}
	}

	public void moatValuesVerification(boolean isMoatServiceReturned) {
		// Getting the cust_params value
		String custParam = getSpecificKeyFromSecurePubadCall("cust_params");
		if (custParam != null && !custParam.isEmpty()) {

			// Validation Moat key value paris
			verifyMoatValue(custParam, "m_mv", isMoatServiceReturned);

			// Validate m_gv keys value pair
			verifyMoatValue(custParam, "m_gv", isMoatServiceReturned);

			// Validate m_data keys value pair
			verifyMoatValue(custParam, "m_data", isMoatServiceReturned);

			// Validate m_safety keys value pair
			verifyMoatValue(custParam, "m_safety", isMoatServiceReturned);

			// Validate m_categories keys value pair
			verifyMoatValue(custParam, "m_categories", isMoatServiceReturned);

		} else {
			generateFailReport("Cust_params has not been returned the values -- " + custParam);
		}
	}

	public void verifyMoatValue(String cust_param, String mValue, boolean isMoatServiceReturned) {
		if (cust_param.contains(mValue) && isMoatServiceReturned) {
			generatePassReportWithNoScreenShot(mValue + " key has been found in the ad call.");

			// Validate m_mv keys value pair
			String mValuet = StringUtils.substringBetween(cust_param, mValue + "=", "&");
			if (!mValuet.isEmpty() && mValuet != null) {
				generatePassReportWithNoScreenShot(mValue + " value is " + mValuet);
			} else {
				generateFailReport(mValue + " value is " + mValuet);
			}
		} else {
			if (isMoatServiceReturned) {
				generateFailReport(
						mValue + " value does not exists in Ad call where Moat service has been returned the values.");
			} else {
				if (cust_param.contains(mValue) && (!isMoatServiceReturned)) {
					generateFailReport(mValue
							+ " value does exists in Ad call where Moat service has not been returned the values.");
				} else {
					if (!cust_param.contains(mValue) && (!isMoatServiceReturned)) {
						generatePassReportWithNoScreenShot(mValue
								+ " value does not exists in ad call where Moat Server it self did not return the values.");
					}
				}
			}
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return getURLs("AdsSanity.xls", "POS_SZS");
	}
}
