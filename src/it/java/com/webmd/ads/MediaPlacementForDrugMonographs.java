package com.webmd.ads;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import com.relevantcodes.extentreports.LogStatus;
import com.webmd.common.AdsCommon;
import com.webmd.general.common.ExtentTestManager;
import com.webmd.general.common.ReadProperties;
import com.webmd.general.common.UtilityMethods;
import com.webmd.general.common.XlRead;

@Listeners(com.webmd.general.common.Listener.class)
public class MediaPlacementForDrugMonographs extends AdsCommon {

	/*
	 * Verify the following possible sizes for 1122 Ad
	 * 300x250,300x50,320x50,300x400
	 */
	// @Test
	public void verify1122Adsizes(String URL) {

		String[] mediaExpectedSizes = { "300x250", "300x50", "320x50", "300x400", "300x51", "320x51" };
		/*
		 * if(URL.contains("+env+")) { URL = URL.replace("+env+", env); }
		 * getServer().newHar(); getDriver().navigate().to(URL);
		 */
		// String prev_scp = getStdParmValues(getServer(), securepubAds,
		// "prev_scp", "pos=1122").get("prev_scp");
		String prev_scp = getSpecificKeyFromSecurePubadCall("prev_scp");
		System.out.println("PrevScp is: " + prev_scp);
		try {
			Assert.assertTrue(prev_scp.contains("pos=1122"));
			String adSizes = getSpecificKeyFromSecurePubadCall("prev_iu_szs");
			String[] posArray = prev_scp.split("\\|");
			String[] sizesArray = adSizes.split(",");
			for (int i = 0; i < posArray.length; i++) {
				if (posArray[i].equals("pos=1122")) {
					String mediaSizes = sizesArray[i];
					String[] actualSizes = mediaSizes.split("\\|");
					for (String actualSize : actualSizes) {
						if (Arrays.asList(mediaExpectedSizes).contains(actualSize)) {
							ExtentTestManager.getTest().log(LogStatus.INFO, "possible size for 1122 Ad: " + actualSize);
						} else {
							ExtentTestManager.getTest().log(LogStatus.FAIL,
									"1122 positions has unexpected size :" + actualSize);
						}
					}

				}
			}
		} catch (AssertionError e) {
			generateFailReport(UtilityMethods.getException(e));
		}
	}

	@DataProvider
	public Object[][] fetchDrugUrls() throws Exception {

		return XlRead.fetchDataExcludingFirstRow("Ads_test_data.xls", "Drug_Monographs");
		/*
		 * System.out.println("From data provider"); Object[][]
		 * URL=ExcelUtils.getTableArray(System.getProperty("user.dir")+"/" +
		 * ReadProperties.projectLocation+"TestInputs/Ads/Ads_test_data.xlsx",
		 * "Drug_Monographs"); return URL;
		 */
	}

	/*
	 * Verify 1122 Ad is displayed in drug monograph pages on top of the next
	 * button
	 */
	// @Test
	public void verify1122AdPos(String URL) {

		Actions a = new Actions(getDriver());
		if (URL.contains("+env+")) {
			URL = URL.replace("+env+", env);
		}

		String xPathVerificationPoint = "//div[@id='ads-pos-1122']/following-sibling::div[@class='back_next_btn']";
		String nextButton = "//div[@class='back_next_btn']/div[@class='next_btn_drug']/a";
		// getDriver().navigate().to(URL);

		Boolean flag = false;

		try {
			Assert.assertTrue(getDriver().findElement(By.xpath(xPathVerificationPoint)).isDisplayed());
			flag = true;
			ExtentTestManager.getTest().log(LogStatus.PASS, "Ad shown above to the Next button in the first section");
		} catch (AssertionError e) {
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Ad not shown above to the Next button");
		}
		if (flag) {
			List<WebElement> sections = getDriver()
					.findElements(By.xpath("//div[@class='sections-nav mobile-sections-nav']/ul/li"));

			sections.remove(0);

			WebElement lastSection = sections.get(sections.size() - 1);

			sections.remove(sections.size() - 1);

			for (WebElement section : sections) {

				a.moveToElement(section).click().build().perform();

				try {
					Assert.assertTrue(getDriver().findElement(By.xpath(xPathVerificationPoint)).isDisplayed());
					ExtentTestManager.getTest().log(LogStatus.PASS, "Ad shown above to the Next button");
				} catch (AssertionError e) {
					ExtentTestManager.getTest().log(LogStatus.FAIL, "Ad not shown above to the Next button");
				}
			}

			a.moveToElement(lastSection).click().build().perform();

			try {
				Assert.assertTrue(getDriver().findElement(By.xpath("//div[@id='ads-pos-1122']")).isDisplayed());
				ExtentTestManager.getTest().log(LogStatus.PASS, "Ad shown in the last section");
			} catch (AssertionError e) {
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Ad not shown in last section");
			}

		}

	}

	@Test(dataProvider = "fetchDrugUrls")
	public void test(String URL) {
		if (URL.contains("+env+")) {
			URL = URL.replace("+env+", env);
		}
		getDriver().navigate().to(URL);
		getServer().newHar();
		// verify1122Adsizes(URL);
		verify1122AdPos(URL);
	}

	@AfterClass()
	public void quitBrowser() {
		getDriver().quit();
		getServer().stop();
	}
}
