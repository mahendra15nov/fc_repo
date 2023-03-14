package com.webmd.ads.regression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.webmd.common.AdsCommon;

import net.bytebuddy.utility.privilege.GetSystemPropertyAction;

/**
 * Created on 11/09/2019 Version : V 1.0;
 * 
 * TextAds Verification
 * 
 * @author amahendra Created on [11/09/2019] - PPE-213083 - Pos 520 not
 *         expanding around Text Driver
 */
@Listeners(com.webmd.general.common.Listener.class)
public class TextAds extends AdsCommon {

	JavascriptExecutor jse;
	boolean lazyLoad = false;
	String urlSL = " has not found.";

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	@BeforeClass(alwaysRun = true)
	public void openBrowser() {
		login(getProperty("username"), getProperty("password"));
	}

	@AfterMethod(alwaysRun = true)
	public void resetValues() {
		lazyLoad = false;
		urlSL = " has not found.";
	}

	/**
	 * PPE-213083 - Pos 520 not expanding around Text Driver
	 * 
	 * @param url
	 */
	@Test(dataProvider = "medscapeurls", groups = { "TextAds", "AdsSmoke", "AdsRegression", "MobileWeb" })
	public void verifyTextDriverAdExp(String url) {
		getServer().newHar();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD"))
			url = url.replace("medscape", env + "medscape");
		getURL(url);
		waitForAdCallFound();
		StaticWait(10);
		// Verify text Ad pos in Ad call
		if (verifySpecificAdPresenceInSecurePubadCall("520") || verifySpecificAdPresenceInSecurePubadCall("420")) {
			generatePassReportWithNoScreenShot("pos 520/420 is appears in Ad call.");
			for (WebElement ele : getDriver().findElements(By.xpath("//*[@id='ads-pos-520' or @id='ads-pos-420']"))) {
				scrollToWebElement(ele);
				StaticWait(2);
				try {
					String height = ele.getCssValue("height");
					if (height.equals("3px")) {
						generateFailReport("pos 520 / 420 is overlapping.");
					} else {
						generatePassReportWithNoScreenShot("pos 420 / 520 is not overlapping.");
					}
				} catch (NoSuchElementException ee) {
					generatePassReportWithNoScreenShot("pos 420 / 520 is not overlapping.");
				}
			}
			for (WebElement ele : getDriver()
					.findElements(By.xpath("//*[@id='ads-pos-520' or @id='ads-pos-420']/div"))) {
				scrollToWebElement(ele);
				StaticWait(2);
				try {
					String height = ele.getCssValue("height");
					if (height.equals("3px")) {
						generateFailReport("pos 520 / 420 is overlapping.");
					} else {
						generatePassReportWithNoScreenShot("pos 420 / 520 is not overlapping.");
					}
				} catch (NoSuchElementException ee) {
					generatePassReportWithNoScreenShot("pos 420 / 520 is not overlapping.");
				}
			}
			// call_Gallen("TestInput/GalenSpecs/textAds.gspec", "text Ad
			// overlapping");
		} else {
			generateFailReport("pos 520/420 does not appear in Ad call.");
		}
	}

	@DataProvider
	public String[] medscapeurls() {
		return new String[] { "https://www.medscape.com/viewarticle/813519",
				"https://www.medscape.com/viewarticle/858342",
				"https://emedicine.medscape.com/article/1230554-overview",
				"https://emedicine.medscape.com/article/1017296-overview",
				"https://reference.medscape.com/viewarticle/842254", "https://portugues.medscape.com/verartigo/6500011",
				"https://portugues.medscape.com/verartigo/6500124",
				"https://deutsch.medscape.com/artikelansicht/4904905",
				"https://francais.medscape.com/voirarticle/3602381" };
	}
}
