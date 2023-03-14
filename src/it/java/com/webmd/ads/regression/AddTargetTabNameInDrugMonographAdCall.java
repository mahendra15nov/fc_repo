package com.webmd.ads.regression;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/*
 * PPE-190393: Add ability to target tabs on Drug Monograph on Desktop and MobileWeb
 * This feature is applicable for both mobile and desktop
 * When DTM flag is true on Drug monograph page Tab name should pass in the ad call
 */

/*
 * Test Cases
 * - validate tab name on page load
 * - validate tab name in lazyload
 * - validate media net refresh
 * - make sure no impact in sub sections
 * - make sure no impact on drug monograph with DTM flag false
 * - Make sure no impact on non drug monograph pages
 * - Access subsequent section directly on DTM is true page
 */

public class AddTargetTabNameInDrugMonographAdCall extends AdsCommon {

	@Test(dataProvider = "dataProvider", groups = { "testDrugMonograph", "AdsRegression", "Desktop", "MobileWeb" })
	public void validateTabValueTracking(String url) {// String URL
		login(getProperty("username"), getProperty("password"));
		getServer().newHar();
		if (!env.isEmpty() && ((!env.equalsIgnoreCase("PROD"))))
			url = url.replace("medscape", env + "medscape");

		getURL(url);
		waitForAdCallFound();
		boolean flag = true;
		if (isTrue("thisIsDrug")) {
			generateInfoReport("Validating on Drug monograph page");
			if (!getDriver().getCurrentUrl().contains("#")) {
				if (flag)
					generateInfoReport("DTM flag is true in the page");
				else
					generateInfoReport("DTM flag is false/not set");
				int count = 1;
				By tab = By.xpath("//div[@id='dose_tabs']/span[" + count + "]");
				boolean virtualRefresh = false;
				do {
					String tabName = getDriver().findElement(By.xpath("//div[@id='dose_tabs']/span[@class='opentab']"))
							.getText();
					generateInfoReport("Validating" + tabName + " upon page-Load/click");
					validateTabName(tabName, flag, virtualRefresh);
					virtualRefresh = false;
					generateInfoReport("Validating lazy load");
					getServer().newHar();
					scrollTillEnd();
					validateTabName(tabName, flag, virtualRefresh);
					count++;
					try {
						tab = By.xpath("//div[@id='dose_tabs']/span[" + count + "]");
						getServer().newHar();
						generateInfoReport("Clicking on next tab");
						getDriver().findElement(tab).isDisplayed();
						click(tab, "Next tab");
						virtualRefresh = true;
					} catch (Exception e) {
						generateInfoReport("No next tab available");
					}
				} while (virtualRefresh);
			} else {
				generateInfoReport("Page is Drug Monograph and DTM is true. But Dosing section is not selected.");
				validateNonPage(getSpecificKeyFromSecurePubadCall("cust_params"));
			}
		} else {
			generateInfoReport("Page is not Drug monograph");
			validateNonPage(getSpecificKeyFromSecurePubadCall("cust_params"));
		}
	}

	@Test(dataProvider = "dataProvider", groups = { "testDrugMonograph", "AdsRegression", "Desktop", "MobileWeb" })
	public void navigateBetweenSubSections(String url) {
		login(getProperty("username"), getProperty("password"));
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");

		getURL(url);
		waitForAdCallFound();
		Boolean DTMFlag = true;
		if (!isTrue("textDriverOptimized") && !isTrue("thisIsDrug"))
			generateSkipReport("DTM Flag is not set/Page is not drug Skipping the test case");
		else {
			By tab = By.xpath("//div[@id='dose_tabs']/span[1]");
			validateTabName(getDriver().findElement(tab).getText(), DTMFlag, false);
			getServer().newHar();
			tab = By.xpath("//div[@id='dose_tabs']/span[2]");
			getDriver().findElement(tab).click();
			generateInfoReport("Clicked on Pediatric tab");
			validateTabName(getDriver().findElement(tab).getText(), DTMFlag, true);
			getServer().newHar();
			generateInfoReport("Clicking on next button to validate next sub section");
			getDriver().findElement(By.xpath("//div[@class='next_btn_drug']/a")).click();
			validateNonPage(getSpecificKeyFromSecurePubadCall("cust_params"));
			generateInfoReport("Selecting back Dosage and Drugs section");
			getServer().newHar();
			getDriver().findElement(By.xpath("//div[contains(@class,'sections-nav')]//a[contains(text(),'Dosing')]"))
					.click();
			String tabSelected = getDriver().findElement(By.xpath("//div[@id='dose_tabs']/span[@class='opentab']"))
					.getText();
			generateInfoReport(tabSelected + " tab is selected");
			validateTabName(tabSelected, DTMFlag, true);
		}
	}

	@DataProvider
	public String[] dataProvider() {
		return new String[] { "https://reference.medscape.com/drug/oralair-grass-pollens-allergen-extract-999883",
				"https://reference.medscape.com/drug/odactra-house-dust-mite-immunotherapy-1000138",
				"https://reference.medscape.com/drug/epogen-procrit-epoetin-alfa-342151",
				"https://reference.medscape.com/drug/neumega-interleukin-11-oprelvekin-342165" };
	}

	public String getExpectedTabValue(String tabName) {
		tabName = tabName.toLowerCase();
		switch (tabName) {
		case "pediatric":
			return "ped";
		case "adult":
			return "adult";
		default:
			return "ger";
		}
	}

	public String getTabNameFromCustParams(String custParams) {
		return StringUtils.substringBetween(custParams, "medsec=", "&");
	}

	public void validateVP(String custParams, boolean isVirtaulPageRefresh) {
		try {
			if (isVirtaulPageRefresh)
				Assert.assertTrue(custParams.contains("&vp=1"));
			else
				Assert.assertTrue(custParams.contains("&vp=0"));
			generatePassReportWithNoScreenShot("VP value loaded properly");
		} catch (AssertionError e) {
			if (isVirtaulPageRefresh)
				generateFailReport("VP value loaded wrong in pageload ad call: " + custParams);
			else
				generateFailReport("VP value loaded wrong in virtual pageload ad call: " + custParams);
		}
	}

	public void validateTabName(String tabName, boolean DTMFlag, boolean isVirtualPageRefresh) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Har har = getServer().getHar();
		int count = 0;

		List<HarEntry> result = har.getLog().getEntries();
		for (HarEntry harEntry : result) {
			String url = harEntry.getRequest().getUrl();
			if (url.contains("securepubads.g.doubleclick.net/gampad/ads?")) {
				count++;
				generateInfoReport("Validating Ad call " + count);
				List<HarNameValuePair> queryParams = harEntry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().equals("cust_params")) {
						String cust_params = harNameValuePair.getValue();

						String tabValue = getTabNameFromCustParams(cust_params);
						if (DTMFlag)
							try {
								validateVP(cust_params, isVirtualPageRefresh);
								Assert.assertEquals(tabValue, getExpectedTabValue(tabName));
								generatePassReportWithNoScreenShot("Tab value tracked under cust_params as expected");
							} catch (AssertionError e) {
								generateFailReport("Tab value not tracked as expected for " + tabName
										+ " value tracked is " + tabValue + "\nCustParams: " + cust_params);
							}
						else
							try {
								validateNonPage(cust_params);
								Assert.assertEquals(tabValue, null);
								generatePassReportWithNoScreenShot(
										"Value not tracked in cust_params while DTM flag is false");
							} catch (AssertionError e) {
								generateFailReport("Value tracked under cust_params when DTM flag is false/not set");
							}
					}
				}
			}
		}
		if (count == 0) {
			generateInfoReport("No Ad call loaded");
		}
	}

	// get DTM flag
	public boolean getDTMFlag() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		boolean flag = false;
		try {
			String value = jse.executeScript("return tab_target").toString();
			if (value.contains("true"))
				flag = true;
		} catch (Exception e) {
			generateInfoReport("Exception while executing console command");
		}
		return flag;
	}

	public void validateNonPage(String custParams) {
		try {
			Assert.assertFalse(custParams.contains("medsec="));
			generatePassReportWithNoScreenShot("Medsec key not loaded under cust_params");
		} catch (AssertionError e) {
			generateFailReport("Medsec key loaded in cust_params: " + custParams);
		} catch (NullPointerException e) {
			generateInfoReport("cust_params is null");
		}

	}

}
