package com.webmd.ads.regression;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.common.AdsConstantns;

/**
 * @author sandeep.gowada
 * 
 *         PPE-219375: Pos 141 not showing up on all Language Edition pages
 *
 */
public class Verify141AllLanguageEditions extends AdsCommon {

	@Test(dataProvider = "allLanguagePages", groups = { "141AllLanguages", "Desktop" })
	public void verify141(String inputURL) {

		setDescription(
				"Verify that ad pos 141 is present in the ad call and is loaded when visiting a page on specified language editions");
		getDriver();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			inputURL = inputURL.replace("medscape", env + "medscape");
		}

		login("infosession33", "medscape");
		getServer().newHar();
		getDriver().get(inputURL);
		generateInfoReport("Loaded: " + inputURL);

		if (verifySpecificCallPresence(AdsConstantns.AD_CALL) && !is404(getDriver().getTitle())) {

			if (verifySpecificAdPresenceInSecurePubadCall("141")) {
				generatePassReport("141 is present in the page loaded ad call");
				check141OnPage();
			}
			while (scrollTillNextLazyLoadFound()) {

				if (verifySpecificAdPresenceInSecurePubadCall("141")) {
					generatePassReport("141 is present in the ad call");
					check141OnPage();
					break;
				}
			}

		}

	}

	private void check141OnPage() {

		String locator = "//div[contains(@id,'ads-pos-141') or contains(@id,'ads-af-pos-141')]";
		if (!isCollapserServed("141")) {
			try {
				WebElement element141 = getDriver().findElement(By.xpath(locator));
				String style = element141.getAttribute("style");
				String classname = element141.getAttribute("classname");
				if (!style.equalsIgnoreCase("display: none")) {
					generatePassReport("141 loaded on page with class:" + classname + " and style: " + style);

				} else {
					generateInfoReport("141 loaded on page with class:" + classname + " and style: " + style);
				}

			} catch (NoSuchElementException e) {
				generateFailReport("141 not present in the DOM");
			}
		}

	}

	@DataProvider
	public String[] allLanguagePages() {

		return getURLs("AllLanguageEditions-141.xls", "medscapeURL");
	}

}
