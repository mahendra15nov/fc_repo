package com.webmd.ads;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.webmd.common.AdsCommon;
import com.webmd.general.common.XlRead;

/*
 * PPE-187347 : ReCAP: Move 1122 / 1909 to utilize default ViewArticle logic
 * This feature is only applicable for mobile web
 */
public class RecapMove1122_1909_ToUtilizeDefaultViewArticlePage extends AdsCommon {

	By validatePositions = By.xpath("//div[@id='article-content']/p[1]/following-sibling::*[position()=1][name()='div']/following-sibling::*[position()=1][name()='div']/following-sibling::*[position()=1][name()='p']");
	By firstAdPos = By.xpath("//div[@id='article-content']/p[1]/following-sibling::*[position()=1][name()='div']");
	By secondAdPos = By.xpath("//div[@id='article-content']/*[position()=3][name()='div']");

	/*
	 * PPE-193699: Verify whether 1909 ad loaded immediately after 1122 position and 1122 is loaded after first content tag or not on recap pages
	 */

	@Test (dataProvider = "dataProvider", groups = {"testRecap"})
	public void testAdPositions(String URL){
		getDriver();
		if(breakPoint.contains("1")){
			login("infosession33", "medscape");
			getDriver().get(URL);
			try{
				getDriver().findElement(validatePositions).isDisplayed();
				generatePassReportWithNoScreenShot("Ad position loaded as expected");
				//validating first position
				String firstPosition = null, secondPosition = null, space = null;
				try{
					firstPosition = getDriver().findElement(firstAdPos).getAttribute("id");
					Assert.assertTrue(firstPosition.contains("1122"));
					generatePassReportWithNoScreenShot("1122 loaded after first content tag");
				}catch(AssertionError e){
					generateFailReport(firstPosition+" loaded after the first content tag");
				}
				//validating second position
				try{
					secondPosition = getDriver().findElement(secondAdPos).getAttribute("id");
					Assert.assertTrue(secondPosition.contains("1909"));
					generatePassReportWithNoScreenShot("1909 loaded after second content tag");
				}catch(AssertionError e){
					generateFailReport(secondPosition+" loaded after the second content tag");
				}

				//validating space between ad positions
				try{
					space = getDriver().findElement(By.xpath("//div[@id='ads-pos-1122']")).getCssValue("margin-bottom");
					Assert.assertTrue(space.contains("15px"));
					generatePassReportWithNoScreenShot("Bottom space after 1122 is 15px");
				}catch(AssertionError e){
					generateFailReport("Bottom space after 1122 is "+space);
				}

			}catch(NoSuchElementException e){
				generateFailReport("Ad positions not loaded as expected");
			}
		}else{
			generateSkipReport("Feature is available only for Mobile break point");
		}

	}

	@DataProvider
	public  String[][] dataProvider() {
		if(env.contains("staging"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "Staging");
		else if(env.contains("qa00"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "QA00");
		else if(env.contains("qa01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "QA01");
		else if(env.contains("dev01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "Dev01");
		else
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "PROD");
	}
	
	@DataProvider
	public  String[][] dataProviderNonRecap() {
		if(env.contains("staging"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "Staging-NonRecap");
		else if(env.contains("qa00"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "QA00-NonRecap");
		else if(env.contains("qa01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "QA01-NonRecap");
		else if(env.contains("dev01"))
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "Dev01-NonRecap");
		else
			return XlRead.fetchDataExcludingFirstRow("TestData/recapData.xls", "PROD-NonRecap");
	}
	
	@Test(dataProvider = "dataProviderNonRecap", groups = {"testRecap"})
	public void validateNonRecapPages(String URL){
		getDriver();
		if(breakPoint.contains("1")){
			login();
			getDriver().get(URL);
			try{
				getDriver().findElement(validatePositions).isDisplayed();
				generateFailReport("Both 1122 and 1909 displayed in non recap page");
			}catch(NoSuchElementException e){
				generatePassReportWithNoScreenShot("1122 and 1909 not displayed one by one in non recap page");
				try{
					String div = getDriver().findElement(firstAdPos).getAttribute("id");
					generateFailReport(div+" displayed after first content tag in non recap page");
				}catch(NoSuchElementException e1){
					generatePassReportWithNoScreenShot("1122 not loaded after first content tag on non recap page");
				}
			}
		}else
			generateSkipReport("Feature is applicable only for Mobile breakpoint");
	}

}
