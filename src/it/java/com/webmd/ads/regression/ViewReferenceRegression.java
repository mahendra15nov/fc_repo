package com.webmd.ads.regression;

import java.util.List;

import org.openqa.selenium.By;
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
 * @author tnamburi This class is to perform
 */

public class ViewReferenceRegression extends AdsCommon {

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
				if (count > 1)
					generateInfoReport("Lazy load ad call loaded");
				else
					generateInfoReport("Ad call loaded");
			}
		}
		if (count == 0)
			generateFailReport("No Ad call loaded after event performed, URL" + getDriver().getCurrentUrl());
		else
			generatePassReport("Ad call/calls loaded after event performed");
	}

	@Test(dataProvider = "dataProviderReferenceArticles", groups = { "testDrugMonograph", "AdsRegression", "Desktop",
			"MobileWeb" })
	public void validateReferenceArticlePage(String URL) {// String URL
		login(getProperty("username"), getProperty("password"));
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			URL = URL.replace("medscape", env + "medscape");
		
		getURL(URL);
		generateInfoReport("Validating after page load");
		validateAdCall();

		List<WebElement> subTabs = getDriver().findElements(subTabsLocator);
		generateBoldReport("Validating all the sub tabs");
		for (WebElement subTab : subTabs) {
			getServer().newHar();
			generateInfoReport("Clicking sub tab: " + subTab.findElement(By.xpath("./a")).getText());
			subTab.click();
			scrollTillEnd();
			scrollBottomToTop();
			validateAdCall();
		}
	}

	@DataProvider
	public String[][] dataProviderReferenceArticles() {
		return XlRead.fetchDataExcludingFirstRow("AdsRegression.xls", "ReferenceArticles");
	}

}
