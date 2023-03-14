package com.webmd.ads;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.general.common.XlRead;

/**
 * 
 * @author tnamburi
 * 
 *         PPE-204359: Specialty Homepages - Add Native Ad Placements
 *         PPE-205679: Create Standard HTML/CSS Template to serve in DFP
 *         PPE-207588: Support both clicks to a microsite and Brand Alert layer
 *         clicks
 *
 */

public class HeadLineDriverAdPlacementsSpecialityHomePages extends HeadLineAdsCommon {

	By spAdPos622 = By.xpath("//ul[@class='column2']/li[1]");
	By spAdPos722 = By.xpath("//ul[@class='articles column1']/li[3]");
	By spAdPos1622 = By.xpath("//div[@class='section-container']/ul[@class='articles']/li[2]");
	By spAdPos1722 = By.xpath("//div[@class='section-container']/ul[@class='articles']/li[5]");

	/**
	 * 
	 * @param adPosition value
	 * @return true if Ad should load on page, false if Ad should not load on page
	 *         based on the ad collapser and Iframe values in the div
	 */
	private boolean shouldAdLoadOnPage(String pos) {
		boolean value = false;
		String mainWindow = getDriver().getWindowHandle();
		try {
			// make sure to connect Iframe
			WebElement iFrame = getDriver().findElement(By.xpath("//div" + pos + "//iframe"));
			try {
				getDriver().switchTo().frame(iFrame);
				getDriver().findElement(By.xpath("//script[contains(@src,'collapse-ad.js')]"));
				generateInfoReport("Collapser loaded, hence Ad shouldn't show in front end");
				value = false;
			} catch (NoSuchElementException e) {
				generateInfoReport("Collapser is not loaded for ad position, hence Ad should be loaded on page");
				value = true;
			}
		} catch (NoSuchElementException e) {
			generateInfoReport("Iframe is not loaded in the ad pos div, hence blank ad should load on page");
			value = false;
		} catch (Exception e) {
			value = false;
			generateInfoReport("Exception while checking collapser: " + e.toString());
		}
		getDriver().switchTo().window(mainWindow);
		return value;
	}

	@Test(enabled = true, dataProvider = "dataProviderHomePages", groups = { "testHeadLineAds" })
	public void testSpecialityHomePage(String homePage, String name) {
		getDriver();
		initiateAdPlacementsOnPage(true);
		login();
		getServer().newHar();
		String URL = "https://www." + env + "medscape.com" + homePage;
		generateInfoReport("Opeing " + name + " page");
		getDriver().get(URL);
		try {
			getDriver().findElement(By.xpath("//h2[contains(text(), 'Latest News')]")).isDisplayed();
			// ad positions are expected in page loaded ad call, hence passing true
			verifyAdCallForPositions(true, adPlacementsOnPage);
			/*
			 * scrollTillEnd(); //ad positions are not expected in lazy loaded ad call,
			 * hence passing false verifyAdCallForPositions(false, adPlacementsOnPage);
			 */

			if (breakPoint.equals("1")) {
				verifyAdPlacementOnPage("1622", spAdPos1622);
				verifyAdPlacementOnPage("1722", spAdPos1722);

			} else {
				verifyAdPlacementOnPage("622", spAdPos622);
				verifyAdPlacementOnPage("722", spAdPos722);

			}

			for (String pos : adPlacementsOnPage) {
				generateInfoReport("Validating Ad position attributes " + pos);
				try {
					validateProperty(pos, "Head Line/Title", "//div[@class='headline-title']", "font|Roboto Condensed",
							"fontSize|20px", "lineHeight|24px", "hex|rgba(51, 49, 50, 1)", "marginBottom|5px");
					validateProperty(pos, "Job Code", "//a[@class='headline-job-code-title']",
							"font|proxima_nova_rgregular, sans-serif", "fontSize|14px", "lineHeight|18px",
							"hex|rgba(118, 118, 116, 1)");
					validateProperty(pos, "IFI label", "//span[@class='headline-ifi-label']",
							"font|proxima_nova_rgregular, sans-serif", "fontSize|14px", "lineHeight|18px",
							"hex|rgba(118, 118, 116, 1)");
					validateClickOnHeadline(pos, headLineLabel);
					validateClickOnHeadline(pos, headLineTitle);
					validateClickOnHeadline(pos, headLinejobCode);
					validateAdvertisementLabel(pos);
				} catch (NoSuchElementException e) {
					generateInfoReport(pos + " is not loaded on page");
				}
			}
		} catch (NoSuchElementException e) {
			generateSkipReport("Latest section not shown on page");
		}

	}

	public void testCustomLink(WebElement ele) {
		String window = getDriver().getWindowHandle();
		getDriver().switchTo().frame(ele);
		try {
			WebElement customLink = getDriver().findElement(By.xpath("//a[@class='headline-second-link']"));
			String font = null;
			String fontSize = null;
			String lineHeight = null;
			String hex = null;
			try {
				// getting the values
				font = customLink.getCssValue("font-family");
				fontSize = customLink.getCssValue("font-size");
				hex = customLink.getCssValue("color");
				lineHeight = customLink.getCssValue("line-height");

				// comparing the values with expected
				compareTwoStrings(font, "proxima_nova_rgregular, sans-serif", "font name");
				compareTwoStrings(fontSize, "14px", "font size");
				compareTwoStrings(lineHeight, "18px", "lineHeight");
				compareTwoStrings(hex, "rgba(0, 124, 176, 1)", "hex/color"); // #767674
				try {
					String linkText = customLink.getText();
					linkText.isEmpty();
					generatePassReportWithNoScreenShot("Custom link loaded properly");
					String exitURL = customLink.getAttribute("href");
					exitURL = StringUtils.substringAfter(exitURL, "$adurl=");
					exitURL = exitURL.replace("http:", "https:");
					customLink.click();
					getDriver().switchTo().window(window);
					String currentUrl = getDriver().getCurrentUrl();
					Assert.assertTrue(currentUrl.contains(exitURL));
					generatePassReportWithNoScreenShot("Clicking on Custom link navigated properly");
				} catch (NullPointerException e) {
					generateInfoReport("Custom link not shown on page");
				} catch (AssertionError e) {
					generateFailReport("Clicking on custom link not navigated properly");
				} catch (Exception e) {
					generateInfoReport("Exception while clicking on custom link " + e.toString());
				}
			} catch (Exception e) {
				generateFailReport("Exception while validating Custom Link " + e.toString());
			}
		} catch (NoSuchElementException e) {
			generateInfoReport("Custom link not shown on page");
		}

	}

	@Test(enabled = true, dataProvider = "dataProviderHomePages", groups = { "testHeadLineAds" })
	public void testCustomTemplate(String homePage, String name) {
		getDriver();
		initiateAdPlacementsOnPage(true);
		login();
		String URL = "https://www." + env + "medscape.com" + homePage;
		generateInfoReport("Opeing " + name + " page");
		getDriver().get(URL);
		for (String pos : adPlacementsOnPage) {
			generateInfoReport("Validating custom link for " + pos);
			try {
				WebElement ele = getDriver().findElement(By.xpath("//li[@id='ads-pos-" + pos + "']//iframe"));
				ele.isDisplayed();
				testCustomLink(ele);
			} catch (NoSuchElementException e) {
				generateInfoReport(pos + " is not loaded on page");
			}
		}
	}

	@DataProvider
	public String[][] dataProviderHomePages() {
		return XlRead.fetchDataExcludingFirstRow("TestData/homePagesNP.xls", "Sheet1");// "us"
	}

}
