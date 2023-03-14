package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.general.common.XlRead;

/**
 * 
 * @author tnamburi PPE-204372: N&P article pages - Add Native Ad Placements
 *
 */

public class HeadLineDriverNPArticlePage extends HeadLineAdsCommon {

	By npAdPos622 = By.xpath("//div[@id='rel-recommend']/ul/li[2]");

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
			WebElement iFrame = getDriver().findElement(By.xpath("//div" + pos + "//iframe"));// make sure to connect
																								// Iframe
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

	@Test(dataProvider = "dataProviderNPArticles", groups = { "testHeadLineAds" })
	public void testNNPArticlePage(String URL) {
		getDriver();
		initiateAdPlacementsOnPage(false);
		login();
		getServer().newHar();
		getDriver().get(URL);
		verifyAdCallForPositions(true, adPlacementsOnPage);
		scrollTillEnd();
		// ad positions are not expected in lazy loaded ad call, hence passing false
		verifyAdCallForPositions(false, adPlacementsOnPage);

		try {
			WebElement recomnnendWidget = getDriver().findElement(By.xpath("//div[@id='rel-recommend']"));
			
			recomnnendWidget.isDisplayed();
			scrollToWebElement(recomnnendWidget);
			generateInfoReport("Recommendations widget available on page");
			verifyAdPlacementOnPage("622", npAdPos622);

			validateProperty("622", "IFI label", "//span[@class='headline-ifi-label']",
					"font|proxima_nova_rgregular, sans-serif", "fontSize|12px", "lineHeight|16px",
					"hex|rgba(118, 118, 116, 1)");
			validateProperty("622", "Head Line / Title", "//div[@class='headline-title']", "font|proxima_nova_rgbold, sans-serif",
					"fontSize|15px", "lineHeight|18px", "hex|rgba(42, 42, 42, 1)", "marginBottom|5px");
			validateProperty("622", "Job code", "//a[@class='headline-job-code-title']", "font|proxima_nova_rgregular, sans-serif",
					"fontSize|14px", "lineHeight|18px", "hex|rgba(118, 118, 116, 1)");
			validateProperty("622", "Ad Position sarround padding", "//div[@class='content']", "5px");
			
			validateClickOnCustomLinks("622");

		} catch (NoSuchElementException e) {
			generateSkipReport("Recommendations section not available");
			addScreenShotToReport();
		}

	}

	@DataProvider
	public String[][] dataProviderNPArticles() {
		return XlRead.fetchDataExcludingFirstRow("TestData/NewsAndPerspectiveArticles.xls", "DEV01");
	}

}
