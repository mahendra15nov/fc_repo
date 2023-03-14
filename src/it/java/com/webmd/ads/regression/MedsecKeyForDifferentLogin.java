package com.webmd.ads.regression;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.general.common.XlRead;

public class MedsecKeyForDifferentLogin extends AddTargetTabNameInDrugMonographAdCall {

	@Test(dataProvider = "dataProvider", groups = { "testDrugMonograph", "AdsRegression", "Desktop", "MobileWeb" })
	public void testDrugMonographPage(String credential) {
		getDriver();
		adsLogin(credential);
		By selectedTab = By.xpath("//div[@id='dose_tabs']/span[@class='opentab']");
		String[] urls = super.dataProvider();
		for (int i = 0; i < urls.length; i++) {
			generateInfoReport("Validating the URL: " + urls[i]);
			getServer().newHar();
			String URL = urls[i];
			if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
				URL = URL.replace("medscape", env + "medscape");

			getURL(URL);
			waitForAdCallFound();
			String currentDosageTab = getDriver().findElement(selectedTab).getText();
			generateInfoReport("Current selected tab is :" + currentDosageTab);
			validateTabName(currentDosageTab, true, false);
			getServer().newHar();
			scrollTillEnd();
			validateTabName(currentDosageTab, true, false);
		}

	}

	@DataProvider
	public String[] dataProvider() {
		return new String[] { "anon", "infosession33,medscape", "uspediatrician@gmail.com,medscape" };
	}

}
