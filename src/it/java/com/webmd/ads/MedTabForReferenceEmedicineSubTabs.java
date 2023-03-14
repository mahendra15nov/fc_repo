package com.webmd.ads;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;
import com.webmd.general.common.XlRead;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

/**
 * 
 * @author tnamburi PPE-190257: Add ability to target sub-pages on Drug
 *         Monograph / Emedicine Template Make sure subtab value in each page
 *         Make sure medsec value
 *
 */

public class MedTabForReferenceEmedicineSubTabs extends AdsCommon {
	By subTabsLocator = By
			.xpath("//div[contains(@class,'sections-nav')]//li[contains(@class,'no_sub') and not(contains(@class,'current')) "
					+ "and not(contains(@style,'display: none'))]");

	private void validateAdCall() {
		Har har = getServer().getHar();
		List<HarEntry> entries = har.getLog().getEntries();
		int count = 0;
		for (HarEntry entry : entries) {
			if (entry.getRequest().getUrl().contains(AdsConstantns.AD_CALL)) {
				count++;
				if (count == 1)
					generateInfoReport("Validating Ad call after page load/click event performed");
				else
					generateInfoReport("Validating lazyload ad call");
				List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
				for (HarNameValuePair harNameValuePair : queryParams) {
					if (harNameValuePair.getName().trim().equalsIgnoreCase("cust_params")) {
						String custParams = harNameValuePair.getValue();
						try {
							String url = getDriver().getCurrentUrl();
							String[] urlSplit = url.split("#");
							if (urlSplit.length > 1)
								Assert.assertTrue(custParams.contains("medtab=" + urlSplit[1]));
							else
								Assert.assertTrue(custParams.contains("medtab=0"));
							generatePassReportWithNoScreenShot("Medtab value tracked properly");
						} catch (AssertionError e) {
							generateFailReport("Medtab value not tracked properly," + getDriver().getCurrentUrl()
									+ " cust_params: " + custParams);
						} catch (Exception e) {
							generateSkipReport("Exception while validating cust_params" + e.toString());
						}

					}
				}
			}
		}
		if (count == 0)
			generateSkipReport("No Ad call loaded after event performed, URL" + getDriver().getCurrentUrl());
	}

	private void validateMobileEmedicine() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		int height = Integer.parseInt(jse.executeScript("return document.body.scrollHeight").toString());
		height -= 10000;
		int count = 0;
		while (height > 1000) {
			getServer().newHar();
			jse.executeScript("window.scrollBy(0, 100)");
			height -= 100;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			Har har = getServer().getHar();
			List<HarEntry> entries = har.getLog().getEntries();
			count = 0;

			for (HarEntry entry : entries) {
				if (entry.getRequest().getUrl().contains(AdsConstantns.AD_CALL)) {
					count++;
					List<HarNameValuePair> queryParams = entry.getRequest().getQueryString();
					for (HarNameValuePair harNameValuePair : queryParams) {
						if (harNameValuePair.getName().trim().equalsIgnoreCase("cust_params")) {
							String custParams = harNameValuePair.getValue();
							try {
								String url = getDriver().getCurrentUrl();
								String[] urlSplit = url.split("#");
								if (urlSplit.length > 1)
									Assert.assertTrue(custParams.contains("medtab=" + urlSplit[1]));
								else
									Assert.assertTrue(custParams.contains("medtab=0"));
								generatePassReportWithNoScreenShot("Medtab value tracked properly");
							} catch (AssertionError e) {
								generateFailReport("Medtab value not tracked properly," + getDriver().getCurrentUrl()
										+ " cust_params: " + custParams);
							} catch (Exception e) {
								generateSkipReport("Exception while validating cust_params" + e.toString());
							}
						}
					}
				}
			}
			if (count == 0) {
				generateInfoReport("No Ad call loaded, scrolling further");
			}
		}
	}

	@Test(dataProvider = "dataProviderReferenceArticles", groups = { "medTabTest" })
	public void validateMedTab(String url) {//
		getDriver();
		login();
		getServer().newHar();
		getDriver().get(url);
		generateInfoReport("Validating after page load");
		validateAdCall();
		List<WebElement> subTabs;

		if (url.contains("emedcine") && breakPoint.equals("1")) {
			validateMobileEmedicine();
			subTabs = getDriver().findElements(subTabsLocator);
			generateBoldReport("Validating all the sub tabs");
			for (WebElement subTab : subTabs) {
				if (!subTab.getAttribute("class").contains("link")) {
					getServer().newHar();
					generateInfoReport("a tab value: " + subTab.findElement(By.xpath(".//a")).getText());
					scrollToWebElement(subTab);
					subTab.click();
					validateAdCall();
				}
			}
		} else {
			subTabs = getDriver().findElements(subTabsLocator);
			generateBoldReport("Validating all the sub tabs");
			for (WebElement subTab : subTabs) {
				if (!subTab.getAttribute("class").contains("link")) {
					getServer().newHar();
					generateInfoReport("a tab value: " + subTab.findElement(By.xpath(".//a")).getText());
					subTab.click();
					scrollTillEnd();
					scrollBottomToTop();
					validateAdCall();
				}
			}
		}

	}

	@DataProvider
	public String[][] dataProviderReferenceArticles() {
		if (env.contains("dev01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/viewReferenceArticles.xls", "DEV01");
		else if (env.contains("staging"))
			return XlRead.fetchDataExcludingFirstRow("TestData/viewReferenceArticles.xls", "STAGING");
		else if (env.contains("qa01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/viewReferenceArticles.xls", "Sheet2");
		else
			return XlRead.fetchDataExcludingFirstRow("TestData/viewReferenceArticles.xls", "PROD");
	}

}
