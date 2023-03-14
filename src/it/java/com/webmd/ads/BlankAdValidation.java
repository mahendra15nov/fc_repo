package com.webmd.ads;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;
import com.webmd.general.common.XlRead;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/*
 * PPE-196366: Blank ad class is not removed on virtual page refreshes
 */

public class BlankAdValidation extends AdsCommon {
	// Input to this method should be ad postion id, it will send both style and
	// class name as output
	private String[] getAdTypeFromAdDiv(String adPos) {
		String adType[] = null;
		String locator = "//div[contains(@id,'ads-pos-" + adPos + "')]";
		try {
			adType[0] = getDriver().findElement(By.xpath(locator)).getAttribute("class");
			adType[1] = getDriver().findElement(By.xpath(locator)).getAttribute("style");
		} catch (NoSuchElementException e) {
			generateInfoReport("Locator issue for position: " + adPos + "locator: " + locator);
		}
		return adType;
	}

	// Make sure start a new har before calling the method
	private void validateAdsLoadedInAdCall() {
		try {
			Assert.assertTrue(verifySpecificCallPresence(AdsConstantns.AD_CALL));
			String prevScp = getSpecificKeyFromSecurePubadCall("prev_scp");
			List<String> positions = getPositionsFromPrevScp(prevScp);
			generateInfoReport("PREV_SCP: " + prevScp);
			String cust_params = getSpecificKeyFromSecurePubadCall("cust_params");
			if (cust_params.contains("&vp=1"))
				generateInfoReport("Ad call is related to virtual page refresh");
			else
				generateInfoReport("Ad call is related to page load/refresh");
			for (String pos : positions) {
				String locator = "//div[contains(@id,'ads-pos-" + pos + "')]";
				WebElement adPos;
				try {
					adPos = getDriver().findElement(By.xpath(locator));
					String adType = validateAdPosOnPage(adPos);
					generateInfoReport(adType + " loaded for the position " + pos);
				} catch (NoSuchElementException e) {
					generateInfoReport(pos + " is in the ad call but not found on page:\nCustParams: " + cust_params);
				}

			}
		} catch (AssertionError e) {
			generateSkipReport("Ad call not loaded after event triggered");
		}
	}

	private void waitForAdCall() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void validateNextTab() {
		generateInfoReport("Validating Tabs on page if available");
		By tab = By.xpath("//div[@id='dose_tabs']/span[2]");
		;
		boolean flag = false;
		try {

			getServer().newHar();
			getDriver().findElement(tab).isDisplayed();
			// getDriver().findElement(tab).click();
			click(tab, "Pediatric tab");
			waitForAdCall();
			generateInfoReport("Clicked on next tab and validating ads loaded in ad call");
			validateAdsLoadedInAdCall();
			getServer().newHar();
			tab = By.xpath("//div[@id='dose_tabs']/span[1]");
			generateInfoReport("Clicking back the Adult Tab to validate virtual refresh");
			click(tab, "Adult Tab");
			waitForAdCall();
			validateAdsLoadedInAdCall();

			getServer().newHar();
			tab = By.xpath("//div[@id='dose_tabs']/span[3]");
			getDriver().findElement(tab).isDisplayed();
			generateInfoReport("Tab is available, clicking on it");
			click(tab, "Clicking on third tab");
			waitForAdCall();
			generateInfoReport("Validating ads after clicking third tab");
			validateAdsLoadedInAdCall();

			getServer().newHar();
			tab = By.xpath("//div[@id='dose_tabs']/span[1]");
			generateInfoReport("Clicking back the Adult Tab to validate virtual refresh");
			click(tab, "Adult Tab");
			waitForAdCall();
			validateAdsLoadedInAdCall();
		} catch (NoSuchElementException e) {
			generateInfoReport("No Next tab available");
		} catch (Exception e) {
			generateInfoReport("Issue with clicking on tab");
		}
	}

	private void validateNextSections() {
		getServer().newHar();
		generateInfoReport("Validating Sub sections on page if available");
		try {
			getDriver().findElement(By.xpath("//div[contains(@class,'sections-nav')]//li[3]/a")).click();
			generateInfoReport("Selected second section");
			waitForAdCall();
			validateAdsLoadedInAdCall();

			getServer().newHar();

			getDriver().findElement(By.xpath("//div[contains(@class,'sections-nav')]//li[4]/a")).click();
			generateInfoReport("Selected third section");
			waitForAdCall();
			validateAdsLoadedInAdCall();

			getServer().newHar();

			getDriver().findElement(By.xpath("//div[contains(@class,'sections-nav')]//li[2]/a")).click();
			generateInfoReport("Selected First section");
			waitForAdCall();
			validateAdsLoadedInAdCall();
		} catch (NoSuchElementException e) {
			generateInfoReport("No Sub section available");
		} catch (WebDriverException e) {
			generateInfoReport("Unable to click Next section");
		}

	}

	private void validateNextPage() {
		try {
			getServer().newHar();
			Assert.assertTrue(clickNextButton());
			waitForAdCall();
			validateAdsLoadedInAdCall();

			getServer().newHar();
			Assert.assertTrue(clickNextButton());
			waitForAdCall();
			validateAdsLoadedInAdCall();
		} catch (AssertionError e) {
			generateInfoReport("No Next button available");
		} catch (Exception e) {
			generateInfoReport("Exception while validating Next button");
		}
	}

	@Test(dataProvider = "dataProvider", groups = { "testBlankAd" })
	public void validateBlankAd(String URL) {
		login();
		if (URL.contains("env."))
			URL = URL.replace("env.", env);
		getDriver();
		getServer().newHar();
		generateBoldReport("Validating Page Load");
		getDriver().get(URL);
		validateAdsLoadedInAdCall();
		getServer().newHar();
		generateBoldReport("Checking availability of Next Tab");
		validateNextTab();
		generateBoldReport("Checking availability of Next Sections");
		validateNextSections();
		generateBoldReport("Checking availability of Next Pages/Slides");
		validateNextPage();
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/blankAdValidation.xls", "Sheet2");

	}

}
