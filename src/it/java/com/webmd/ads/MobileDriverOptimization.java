package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

public class MobileDriverOptimization extends AdsCommon {

	String[] expectedSizesIfFlagTrue_1520 = "300x254|300x50|320x50".split("|");
	String[] expectedSizesIfFlagTrue_1420 = "300x254|300x50|320x50|2x3|1x12".split("|");
	String[] expectedSizesIfFlagFalse = "320x50|2x3|1x12|300x254".split("|");

	// This method will execute the console command given and return the response in
	// a string format
	public String executeConsoleCommand(String command) {
		String response = null;

		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		try {
			response = jse.executeScript("return " + command).toString();
		} catch (WebDriverException e) {
			e.printStackTrace();
			response = command + " is not defined";
		}
		return response;
	}

	// if the opened page is article this method will return true otherwise false
	public boolean isArticle() {
		if (executeConsoleCommand("_isAnArticle").equals("true"))
			return true;
		else
			return false;
	}

	// if textDriverOptimized is true this method will return true, false otherwise
	public boolean isTextDriverOptimized() {
		if (executeConsoleCommand("textDriverOptimized").equals("true"))
			return true;
		else
			return false;
	}

	@BeforeClass()
	public void beforeClass() {
		login(getProperty("username"), getProperty("password"));
	}

	/*
	 * @BeforeMethod(alwaysRun = true) public void beforeMethod(){ getDriver();
	 * getServer(); }
	 */

	// This method is to validate whether each size loaded once
	public boolean isSizeLoadedOnce(String sizes, String size) {
		if (sizes.split(size).length == 2)
			return true;
		else
			return false;
	}

	// This method is to validate sizes of both positions individually based on flag
	// value/availability
	public void validateSizes(String pos, boolean flag) {
		if (flag)
			generateInfoReport("Validating sizes for " + pos + " and flag is true on the page");
		else
			generateInfoReport("Validating sizes for " + pos + " and flag is false/not set on the page");
		boolean result = true;
		String sizes = getSizesForSpecificPositionFromAdCall(pos);

		try {
			Assert.assertNotEquals(sizes, null);
			generateInfoReport("Sizes for position " + pos + " are " + sizes);

			if (pos.equals("1420") && flag) {
				try {
					Assert.assertEquals(expectedSizesIfFlagTrue_1420.length, sizes.split("|").length);
					generatePassReportWithNoScreenShot("Number of sizes loaded in ad call is correct for 1420");
				} catch (AssertionError e) {
					generateFailReport("Wrong number of sizes loaded in ad call for 1420" + sizes);
				}
				for (String size : expectedSizesIfFlagTrue_1420) {
					if (!sizes.contains(size) && !isSizeLoadedOnce(sizes, size))
						generateFailReport("Size not mentioned in Ad call: " + size);
				}
			} else if (pos.equals("1520") && flag) {
				try {
					Assert.assertEquals(expectedSizesIfFlagTrue_1520.length, sizes.split("|").length);
					generatePassReportWithNoScreenShot("Number of sizes loaded in ad call is correct for 1520");
				} catch (AssertionError e) {
					generateFailReport("Wrong number of sizes loaded in ad call for 1520" + sizes);
				}
				for (String size : expectedSizesIfFlagTrue_1520) {
					if (!sizes.contains(size) && !isSizeLoadedOnce(sizes, size))
						generateFailReport("Size not mentioned in Ad call: " + size);
				}
			} else {

				try {
					Assert.assertEquals(expectedSizesIfFlagFalse.length, sizes.split("|").length);
					generatePassReportWithNoScreenShot("Number of sizes loaded in ad call is correct");
				} catch (AssertionError e) {
					generateFailReport("Wrong number of sizes loaded in ad call: " + sizes);
				}
				for (String size : expectedSizesIfFlagFalse) {
					if (!sizes.contains(size) && !isSizeLoadedOnce(sizes, size))
						generateFailReport("Size not mentioned in Ad call: " + size);
				}
			}

		} catch (AssertionError e) {
			generateInfoReport(pos + " is not loaded in ad call:" + getDriver().getCurrentUrl());
		}
	}

	public boolean validateEvent() {
		try {

			Assert.assertTrue(verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
			generateInfoReport("Ad call made on the page");
			if (isArticle() && isTextDriverOptimized()) {

				validateSizes("1420", true);

				validateSizes("1520", true);
			} else {

				validateSizes("1420", false);

				validateSizes("1520", false);
			}
			return true;
		} catch (AssertionError e) {
			return false;
		}
	}

	@Test(dataProvider = "dataProvider")
	public void test(String URL) {
		boolean hasNextPage = false;
		getDriver();
		if (!env.isEmpty() && !env.equalsIgnoreCase("PROD")) {
			URL = URL.replace("medscape", env + "medscape");
		}
		getServer().newHar();
		getDriver().get(URL);
		int count = 0;
		do {
			getServer().getHar();
			try {
				Assert.assertTrue(validateEvent());
			} catch (AssertionError e) {
				generateFailReport("No ad call made for page load:" + getDriver().getCurrentUrl());
			}

			/*
			 * try{ do{ getServer().newHar(); count++; try{ Thread.sleep(5000);
			 * }catch(InterruptedException e){ e.printStackTrace(); } }while(count < 14 &&
			 * !verifySpecificCallPresence("securepubads.g.doubleclick.net/gampad/ads?"));
			 * generateInfoReport("Validating Media net");
			 * Assert.assertTrue(validateEvent()); }catch(AssertionError e){
			 * generateInfoReport("No Media net refresh is done"); }
			 */
			try {
				getServer().newHar();
				getDriver().findElement(By.xpath("//div[@id='next-section']")).click();
				generateInfoReport("Next page available, validating next page");
				hasNextPage = true;
			} catch (Exception e) {
				generateInfoReport("No Next page available");
				hasNextPage = false;
			}
		} while (hasNextPage);
	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/DTMTestData.xls", "Sheet1");
	}

	@DataProvider
	public String[][] dataProviderNonArticles() {
		return XlRead.fetchDataExcludingFirstRow("TestData/DTMTestData.xls", "Sheet2");
	}

}
