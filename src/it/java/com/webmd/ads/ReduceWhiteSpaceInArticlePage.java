package com.webmd.ads;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

//PPE-164230: N&P: reduce extra white space above what to read next footer 
public class ReduceWhiteSpaceInArticlePage extends AdsCommon {

	By legalBlock = By.xpath("//div[@id='legal_block']");
	By whatToReadNextSection = By.xpath("//div[@id='rel-links-container']/div[@id='rel-links']");

	@BeforeClass(groups = { "removeWhiteSpace"})
	public void beforeClass() {
		login();
	}

	@AfterClass(groups = { "removeWhiteSpace" })
	public void closeBrowser() {
		getDriver().quit();
		getServer().stop();
	}

	private int getLocation(WebElement ele) {
		return ele.getLocation().y;
	}

	private void testSpace() {
		int locationCitation = getLocation(getDriver().findElement(legalBlock));
		int locationReadNext = getLocation(getDriver().findElement(whatToReadNextSection));
		System.out.println("coordinates: " + locationCitation + " , " + locationReadNext);
		int gap = locationReadNext - locationCitation;
		try {
			Assert.assertTrue(gap < 100);
			generatePassReport("Space between Read Next section and Citation is: " + gap);
		} catch (AssertionError e) {
			generateFailReport("Space between Read Next section and Citation is: " + gap);
		}
	}

	// Method to validated space with galen script
	private void testSpace1(String type) {
		if (type.equalsIgnoreCase("coe"))
			call_Gallen("TestInput/GalenSpecs/WhiteSpaceBelowArticleContentCOE.gspec", getDriver().getCurrentUrl());
		else
			call_Gallen("TestInput/GalenSpecs/WhiteSpaceBelowArticleContent.gspec", getDriver().getCurrentUrl());
	}

	@Test(dataProvider = "dataProvider", groups = { "removeWhiteSpace" })
	public void test(String type, String URL) {// String type, String URL
		generateInfoReport("Validating URL of type: " + type);
		getDriver().get(URL);
		boolean flag = true;
		int count = 1;
		do {
			testSpace1(type);
			try {
				getDriver().findElement(By.xpath("//div[@id='next-section']")).isEnabled();
				System.out.println("Next button enabled");
				getDriver().findElement(By.xpath("//div[@id='next-section']")).click();
				count++;
				generateInfoReport("Validating page: " + count);
			} catch (NoSuchElementException e) {
				generateInfoReport("No pagenation");
				flag = false;
			} catch (Exception e) {
				generateInfoReport("Next button not enabled");
				flag = false;
			}
		} while (flag);

	}

	@DataProvider
	public String[][] dataProvider() {
		return XlRead.fetchDataExcludingFirstRow("TestData/AllTypeURLs.xls", "Sheet1");
		// return XlRead.fetchDataExcludingFirstRow("TestData/Test.xls",
		// "test");

		/*
		 * return new String[][] { {
		 * "https://www.medscape.com/viewarticle/893872" },
		 * {"https://www.medscape.com/viewarticle/899865"},
		 * {"https://www.medscape.com/viewarticle/813519"} };
		 */
	}

}
