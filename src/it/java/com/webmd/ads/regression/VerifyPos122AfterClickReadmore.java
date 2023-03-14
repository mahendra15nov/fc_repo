package com.webmd.ads.regression;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

/**
 * @author sandeep.gowada
 * 
 *         PPE-215634: pos 122 Ad is disappear after click Read More option
 *
 */
public class VerifyPos122AfterClickReadmore extends AdsCommon {

	By hidePath = By.xpath("//*[@id='article-content']//span[@class='show-more expanded']");
	By readMore = By.xpath("//*[@id='article-content']//span[@class='show-more']");
	By adpos122Path = By.xpath("//div[@id='ads-pos-122']");

	@Test(dataProvider = "dataProvider", groups = { "Desktop" })
	public void verify122Visibility(String url) {

		setDescription("Verify that pos 122 does not disappear after clicking Read More");
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			url = url.replace("medscape", env + "medscape");
		}
		if (url.contains("registration_ab.do") || url.contains("login")) {
			logout(getProperty("username"));
		}

		getDriver();
		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(url);
		generateInfoReport("Loaded: " + url);
		if (verifySpecificCallPresence(AdsConstantns.AD_CALL)) {

			try {
				generateInfoReport("Checking if ad pos 122 loaded on page load");
				checkAd122();
				workAroundReadMore();
			} catch (NoSuchElementException e) {
				generateFailReport("Ad pos 122 not loaded in the page " + e.getMessage());
			}
		}

	}

	/*
	 * This method will work around Read more option by clicking on it and checking
	 * visibility of pos 122 and then clicks on Hide Transcript to check if pos 122
	 * is not disappeared
	 *
	 */
	private void workAroundReadMore() {

		generateInfoReport("Checking ad pos 122 does not disappear after clicking Readmore");
		try {
			WebElement readMoreButton = getDriver().findElement(readMore);
			generateInfoReport("Clicking on Read more..");
			readMoreButton.click();
			checkAd122();
		}

		catch (NoSuchElementException e) {
			generateInfoReport("Did not find Read more on the page " + e.getMessage());
		}

		generateInfoReport("Checking ad pos 122 does not disappear after clicking Hide Transcript");
		try {
			scrollToObject(hidePath, "");
			try {
				generateInfoReport("Clicking on Hide Transcript");
				JavascriptExecutor executor = (JavascriptExecutor) getDriver();
				executor.executeScript("arguments[0].click();", getDriver().findElement(hidePath));
				scrollToObject(adpos122Path, "");
				checkAd122();
			} catch (Exception e) {
				generateInfoReport("Unable to click on Hide transcript" + e.getMessage());
			}
		}

		catch (NoSuchElementException e) {
			generateInfoReport("Did not find Hide Transcript on the page " + e.getMessage());
		}

	}

	/*
	 * This method will verify if pos 122 is loaded on page
	 */
	private void checkAd122() {

		try {

			WebElement adPos122 = getDriver().findElement(adpos122Path);
			generateInfoReport("Ad pos 122 is available in the DOM");
			if (!(adPos122.getAttribute("class").equalsIgnoreCase("blank-ad"))) {

				try {
					WebElement iFrame122 = getDriver().findElement(By.xpath("//div[@id='ads-pos-122']//iframe"));
					getDriver().switchTo().frame(iFrame122);
					generatePassReport("Ad pos 122 loaded on the page physically");
					getDriver().switchTo().defaultContent();
				} catch (NoSuchElementException e) {
					generateFailReport("Ad pos 122 not loaded in the page " + e.getMessage());
				}
			} else {
				generateInfoReport("Blank ad loaded for pos 122");
			}
		} catch (NoSuchElementException e) {
			generateFailReport("Ad pos 122 not available on page " + e.getMessage());
		}

	}

	@DataProvider
	public String[][] dataProvider() {
		return new String[][] { { "https://odp.medscape.com/viewarticle/897194" },
				{ "https://odp.medscape.com/viewarticle/897167" }, { "https://odp.medscape.com/viewarticle/897185" },
				{ "https://odp.medscape.com/viewarticle/897169" } };
	}
}