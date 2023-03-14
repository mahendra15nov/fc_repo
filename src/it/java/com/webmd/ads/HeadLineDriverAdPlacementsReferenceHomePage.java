package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

/**
 * 
 * @author tnamburi PPE-204369: Reference Homepage - Add Native Ad Placements
 * 
 *         PPE-205679:
 *
 */

public class HeadLineDriverAdPlacementsReferenceHomePage extends HeadLineDriverAdPlacementsSpecialityHomePages {

	By refAdPos622 = By.xpath("//div[@id='featured']/div[@class='section-content']/ul/li[3]");
	By refAdPos1622 = By.xpath("//h1[contains(text(),'Drugs & Diseases')]/following-sibling::*[position()=1]");

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

	@Test(groups = { "testHeadLineAds" })
	public void testReferenceHomePage() {
		getDriver();
		initiateAdPlacementsOnPage(false);
		login();
		getServer().newHar();
		getDriver().get("https://reference." + env + "medscape.com/");
		// ad positions are expected in page loaded ad call, hence passing true
		verifyAdCallForPositions(true, adPlacementsOnPage);
		scrollTillEnd();
		// ad positions are not expected in lazy loaded ad call, hence passing false
		verifyAdCallForPositions(false, adPlacementsOnPage);

		if (breakPoint.equals("1")) {

			verifyAdPlacementOnPage("1622", refAdPos1622);
		} else {

			verifyAdPlacementOnPage("622", refAdPos622);
		}

		for (String pos : adPlacementsOnPage) {
			try {
				validateClickOnHeadline(pos, headLineTitle);
				validateClickOnHeadline(pos, headLineLabel);
				validateClickOnHeadline(pos, headLinejobCode);
				validateAdvertisementLabel(pos);
			} catch (NoSuchElementException e) {
				generateSkipReport(pos + " is not loaded on page");
				// make sure ad collapsed
			}
		}

	}

	@Test(enabled = true, groups = { "testHeadLineAds" })
	public void testCustomTemplateReferencePage() {
		getDriver();
		initiateAdPlacementsOnPage(false);
		login();
		getDriver().get("https://reference." + env + "medscape.com/");

		for (String pos : adPlacementsOnPage) {
			try {
				waitForElement(By.xpath("//li[@id='ads-pos-]" + pos + "']//iframe"));
				WebElement ele = getDriver().findElement(By.xpath("//li[@id='ads-pos-]" + pos + "']//iframe"));
				ele.isDisplayed();
				testCustomLink(ele);
			} catch (NoSuchElementException e) {
				generateInfoReport(pos + " is not loaded on page");
			}
		}
	}

}
